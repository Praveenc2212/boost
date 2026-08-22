package com.example.network

import android.util.Log
import com.example.model.BoostReactionState
import com.example.model.GamePlayState
import com.example.model.Player
import com.example.model.PlayerRoundRank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class LanServer(
    private val scope: CoroutineScope,
    val roomCode: String,
    val roomName: String,
    private val hostPlayer: Player,
    private val onStateUpdated: (List<Player>, GamePlayState, String?) -> Unit
) {
    private val TAG = "LanServer"
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var botTurnJob: Job? = null
    private var boostTimerJob: Job? = null
    private var botBoostTapsJob: Job? = null

    val players = CopyOnWriteArrayList<Player>()
    private val clientWriters = ConcurrentHashMap<String, PrintWriter>()
    private val playerHands = ConcurrentHashMap<String, MutableList<String>>()
    private val boostTapTimestamps = ConcurrentHashMap<String, Long>()

    var playState = GamePlayState()
        private set

    var currentStage: String = "LOBBY"
        private set

    private var currentRound: Int = 1

    init {
        players.add(hostPlayer.copy(isHost = true, isReady = true))
    }

    fun start() {
        serverJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(NetworkProtocol.TCP_GAME_PORT)
                Log.d(TAG, "LanServer started on port ${NetworkProtocol.TCP_GAME_PORT}")

                while (isActive) {
                    val clientSocket = serverSocket?.accept() ?: break
                    handleNewClient(clientSocket)
                }
            } catch (e: Exception) {
                Log.e(TAG, "LanServer error or stopped", e)
            }
        }
    }

    private fun handleNewClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            var playerId: String? = null
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                val writer = PrintWriter(socket.getOutputStream().bufferedWriter(Charsets.UTF_8), true)

                while (isActive) {
                    val line = reader.readLine() ?: break
                    val json = JSONObject(line)
                    val type = json.optString("type")

                    when (type) {
                        NetworkProtocol.TYPE_JOIN_REQUEST -> {
                            val id = json.getString("playerId")
                            val name = json.getString("playerName")
                            playerId = id
                            clientWriters[id] = writer

                            // Check if already in list or room is full
                            if (players.none { it.id == id } && players.size < 6 && currentStage == "LOBBY") {
                                players.add(Player(id = id, name = name, isHost = false, isReady = false))
                                broadcastLobbyState()
                            }
                        }

                        NetworkProtocol.TYPE_TOGGLE_READY -> {
                            val id = json.getString("playerId")
                            val isReady = json.getBoolean("isReady")
                            val index = players.indexOfFirst { it.id == id }
                            if (index >= 0) {
                                players[index] = players[index].copy(isReady = isReady)
                                broadcastLobbyState()
                            }
                        }

                        NetworkProtocol.TYPE_SUBMIT_WORD -> {
                            val id = json.getString("playerId")
                            val word = json.getString("word").trim().uppercase()
                            val index = players.indexOfFirst { it.id == id }
                            if (index >= 0) {
                                players[index] = players[index].copy(
                                    submittedWord = word,
                                    hasSubmittedWord = true
                                )
                                broadcastWordSubmissions()
                                checkAllWordsSubmitted()
                            }
                        }

                        NetworkProtocol.TYPE_PASS_CARD -> {
                            val id = json.getString("playerId")
                            val cardWord = json.getString("cardWord").trim().uppercase()
                            executePassCard(id, cardWord)
                        }

                        NetworkProtocol.TYPE_TRIGGER_BOOST -> {
                            val id = json.getString("playerId")
                            handleTriggerBoost(id)
                        }

                        NetworkProtocol.TYPE_BOOST_TAP -> {
                            val id = json.getString("playerId")
                            val timestamp = json.optLong("timestampMs", System.currentTimeMillis())
                            handlePlayerBoostTap(id, timestamp)
                        }

                        NetworkProtocol.TYPE_NEXT_ROUND -> {
                            startNextRound()
                        }

                        NetworkProtocol.TYPE_LEAVE -> {
                            val id = json.getString("playerId")
                            removePlayer(id)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Client handler exception for $playerId", e)
            } finally {
                playerId?.let { removePlayer(it) }
                try {
                    socket.close()
                } catch (e: Exception) {}
            }
        }
    }

    fun addBotPlayer() {
        if (players.size >= 6 || currentStage != "LOBBY") return
        val botNames = listOf("AeroBot", "CyberBot", "NeonAI", "ViperBot", "ShadowAI", "TitanBot")
        val unusedName = botNames.firstOrNull { name -> players.none { it.name == name } } ?: "Bot-${players.size + 1}"
        val botId = "bot_${System.currentTimeMillis()}_${(100..999).random()}"
        val bot = Player(
            id = botId,
            name = unusedName,
            isHost = false,
            isReady = true,
            isBot = true,
            pingMs = 2L
        )
        players.add(bot)
        broadcastLobbyState()
    }

    fun removePlayer(playerId: String) {
        val index = players.indexOfFirst { it.id == playerId }
        if (index >= 0) {
            players.removeAt(index)
            clientWriters.remove(playerId)
            playerHands.remove(playerId)
            if (currentStage == "LOBBY") {
                broadcastLobbyState()
            } else if (currentStage == "IN_GAME") {
                if (playState.turnPlayerId == playerId && players.isNotEmpty()) {
                    val nextIdx = index % players.size
                    advanceTurnTo(nextIdx)
                }
                broadcastGameState()
            }
        }
    }

    fun startWordSelectionPhase() {
        if (players.size < 2) return
        currentStage = "WORD_SELECTION"
        boostTapTimestamps.clear()

        // Reset submitted words
        for (i in players.indices) {
            val p = players[i]
            if (p.isBot) {
                val used = players.mapNotNull { it.submittedWord.takeIf { w -> w.isNotEmpty() } }
                val pick = GameEngine.WORD_SUGGESTIONS.firstOrNull { it !in used } ?: "CYBER"
                players[i] = p.copy(
                    submittedWord = pick,
                    hasSubmittedWord = true,
                    hasPressedBoost = false,
                    boostPressTimeMs = null
                )
            } else {
                players[i] = p.copy(
                    submittedWord = "",
                    hasSubmittedWord = false,
                    hasPressedBoost = false,
                    boostPressTimeMs = null
                )
            }
        }

        val json = JSONObject().apply {
            put("type", NetworkProtocol.TYPE_START_WORD_SELECTION)
        }
        broadcastRaw(json.toString())
        broadcastWordSubmissions()
        checkAllWordsSubmitted()
    }

    fun hostSubmitWord(word: String) {
        val index = players.indexOfFirst { it.id == hostPlayer.id }
        if (index >= 0) {
            players[index] = players[index].copy(
                submittedWord = word.trim().uppercase(),
                hasSubmittedWord = true
            )
            broadcastWordSubmissions()
            checkAllWordsSubmitted()
        }
    }

    private fun checkAllWordsSubmitted() {
        val allSubmitted = players.all { it.hasSubmittedWord }
        if (allSubmitted && players.size >= 2) {
            scope.launch(Dispatchers.Default) {
                currentStage = "SHUFFLE"
                val shuffleNotice = JSONObject().apply {
                    put("type", NetworkProtocol.TYPE_SHUFFLE_START)
                }
                broadcastRaw(shuffleNotice.toString())
                onStateUpdated(players.toList(), playState, "SHUFFLE")

                delay(2200)
                dealAndStartGame()
            }
        }
    }

    private fun dealAndStartGame() {
        currentStage = "IN_GAME"
        val dealtHands = GameEngine.dealInitialCards(players.toList())
        playerHands.clear()
        dealtHands.forEach { (id, cards) ->
            playerHands[id] = cards.toMutableList()
        }

        // Initialize PlayState
        playState = GamePlayState(
            roundNumber = currentRound,
            turnPlayerIndex = 0,
            turnPlayerId = players[0].id,
            turnPlayerName = players[0].name,
            targetPlayerId = players[1 % players.size].id,
            targetPlayerName = players[1 % players.size].name,
            isFirstRotationComplete = false,
            totalMovesMade = 0,
            boosterPlayerId = null,
            boosterPlayerName = null,
            winningWord = null,
            boostReaction = null
        )

        broadcastGameState()
        checkAndTriggerBotTurn()
    }

    fun hostPassCard(cardWord: String) {
        executePassCard(hostPlayer.id, cardWord)
    }

    private fun executePassCard(playerId: String, cardWord: String) {
        if (currentStage != "IN_GAME") return
        if (playState.turnPlayerId != playerId) return

        val senderIdx = players.indexOfFirst { it.id == playerId }
        if (senderIdx < 0) return

        val receiverIdx = (senderIdx + 1) % players.size
        val receiverPlayer = players[receiverIdx]

        val senderHand = playerHands[playerId] ?: return
        val receiverHand = playerHands[receiverPlayer.id] ?: return

        val cardIndex = senderHand.indexOfFirst { it.equals(cardWord, ignoreCase = true) }
        if (cardIndex < 0) return

        val removedCard = senderHand.removeAt(cardIndex)
        receiverHand.add(removedCard)

        val newTotalMoves = playState.totalMovesMade + 1
        val rotationComplete = playState.isFirstRotationComplete || (newTotalMoves >= players.size)

        var boosterId: String? = null
        var boosterName: String? = null
        var winningWord: String? = null

        // Check if receiver now has 3 matching cards after rotation
        if (rotationComplete && receiverHand.size == 3) {
            val winWord = GameEngine.checkWinningHand(receiverHand)
            if (winWord != null) {
                boosterId = receiverPlayer.id
                boosterName = receiverPlayer.name
                winningWord = winWord
            }
        }

        val nextTurnIdx = receiverIdx
        val targetIdx = (nextTurnIdx + 1) % players.size

        playState = playState.copy(
            roundNumber = currentRound,
            turnPlayerIndex = nextTurnIdx,
            turnPlayerId = players[nextTurnIdx].id,
            turnPlayerName = players[nextTurnIdx].name,
            targetPlayerId = players[targetIdx].id,
            targetPlayerName = players[targetIdx].name,
            isFirstRotationComplete = rotationComplete,
            totalMovesMade = newTotalMoves,
            lastPassedCard = removedCard,
            lastPassedFromPlayer = players[senderIdx].name,
            lastPassedToPlayer = receiverPlayer.name,
            boosterPlayerId = boosterId,
            boosterPlayerName = boosterName,
            winningWord = winningWord
        )

        broadcastGameState()

        // If booster is a bot, bot triggers BOOST after slight natural delay
        if (boosterId != null) {
            val boosterP = players.firstOrNull { it.id == boosterId }
            if (boosterP?.isBot == true) {
                scope.launch(Dispatchers.Default) {
                    delay(800)
                    handleTriggerBoost(boosterId)
                }
            }
        } else {
            checkAndTriggerBotTurn()
        }
    }

    fun hostTriggerBoost() {
        handleTriggerBoost(hostPlayer.id)
    }

    private fun handleTriggerBoost(boosterId: String) {
        if (currentStage != "IN_GAME") return
        val booster = players.firstOrNull { it.id == boosterId } ?: return
        val boosterHand = playerHands[boosterId] ?: return
        val winWord = GameEngine.checkWinningHand(boosterHand) ?: playState.winningWord ?: "MATCH"

        currentStage = "BOOST_ACTIVATED"
        boostTapTimestamps.clear()
        val startEpoch = System.currentTimeMillis()

        // Record booster's initial tap automatically with timestamp 0 offset
        boostTapTimestamps[boosterId] = startEpoch

        val reactionState = BoostReactionState(
            boosterPlayerId = booster.id,
            boosterPlayerName = booster.name,
            winningWord = winWord,
            startTimeEpochMs = startEpoch,
            durationSeconds = 3.8f,
            tappedPlayerIds = listOf(boosterId)
        )

        playState = playState.copy(
            boosterPlayerId = booster.id,
            boosterPlayerName = booster.name,
            winningWord = winWord,
            boostReaction = reactionState
        )

        // Broadcast BOOST_STARTED
        val json = JSONObject().apply {
            put("type", NetworkProtocol.TYPE_BOOST_STARTED)
            put("boosterId", booster.id)
            put("boosterName", booster.name)
            put("winningWord", winWord)
            put("startTimeMs", startEpoch)
            put("durationSeconds", 3.8)
        }
        broadcastRaw(json.toString())
        onStateUpdated(players.toList(), playState, "BOOST_ACTIVATED")

        // Schedule bots to tap with varied human reaction delays (350ms - 2600ms)
        scheduleBotBoostTaps(startEpoch)

        // Start countdown timer for round completion (3.8s)
        boostTimerJob?.cancel()
        boostTimerJob = scope.launch(Dispatchers.Default) {
            delay(3900)
            if (currentStage == "BOOST_ACTIVATED") {
                finishBoostRound()
            }
        }
    }

    private fun scheduleBotBoostTaps(startEpoch: Long) {
        botBoostTapsJob?.cancel()
        botBoostTapsJob = scope.launch(Dispatchers.Default) {
            val bots = players.filter { it.isBot && it.id != playState.boosterPlayerId }
            bots.forEach { bot ->
                launch {
                    val delayMs = (400L..2500L).random()
                    delay(delayMs)
                    if (currentStage == "BOOST_ACTIVATED") {
                        handlePlayerBoostTap(bot.id, startEpoch + delayMs)
                    }
                }
            }
        }
    }

    fun hostBoostTap(timestampMs: Long = System.currentTimeMillis()) {
        handlePlayerBoostTap(hostPlayer.id, timestampMs)
    }

    private fun handlePlayerBoostTap(playerId: String, timestamp: Long) {
        if (currentStage != "BOOST_ACTIVATED") return
        if (boostTapTimestamps.containsKey(playerId)) return // already tapped

        boostTapTimestamps[playerId] = timestamp

        val currentTapped = playState.boostReaction?.tappedPlayerIds?.toMutableList() ?: mutableListOf()
        if (playerId !in currentTapped) {
            currentTapped.add(playerId)
        }

        playState = playState.copy(
            boostReaction = playState.boostReaction?.copy(tappedPlayerIds = currentTapped)
        )

        val json = JSONObject().apply {
            put("type", NetworkProtocol.TYPE_BOOST_REACTION_UPDATE)
            val arr = JSONArray()
            currentTapped.forEach { arr.put(it) }
            put("tappedPlayerIds", arr)
        }
        broadcastRaw(json.toString())
        onStateUpdated(players.toList(), playState, "BOOST_ACTIVATED")

        // If all players have tapped, finish round immediately without waiting for timer!
        if (boostTapTimestamps.size >= players.size) {
            boostTimerJob?.cancel()
            finishBoostRound()
        }
    }

    private fun finishBoostRound() {
        if (currentStage != "BOOST_ACTIVATED") return
        currentStage = "ROUND_RESULTS"
        boostTimerJob?.cancel()
        botBoostTapsJob?.cancel()

        val n = players.size
        val boosterId = playState.boosterPlayerId ?: players[0].id
        val startEpoch = playState.boostReaction?.startTimeEpochMs ?: System.currentTimeMillis()

        // Sort non-booster players by their tap timestamp
        val nonBoosters = players.filter { it.id != boosterId }
        val tappedNonBoosters = nonBoosters
            .filter { boostTapTimestamps.containsKey(it.id) }
            .sortedBy { boostTapTimestamps[it.id] ?: Long.MAX_VALUE }
        val untappedNonBoosters = nonBoosters.filter { !boostTapTimestamps.containsKey(it.id) }

        // Full ranking order:
        // Rank 1: Booster (receives N * 10)
        // Rank 2: 1st tap (receives (N - 1) * 10)
        // Rank 3: 2nd tap (receives (N - 2) * 10)...
        val orderedRanks = mutableListOf<PlayerRoundRank>()

        // 1. Booster Rank 1
        val boosterP = players.first { it.id == boosterId }
        val boosterPoints = n * 10
        val newBoosterTotal = boosterP.totalScore + boosterPoints
        orderedRanks.add(
            PlayerRoundRank(
                rank = 1,
                playerId = boosterP.id,
                playerName = boosterP.name,
                isBooster = true,
                pointsAwarded = boosterPoints,
                cumulativeScore = newBoosterTotal,
                reactionTimeMs = 0L
            )
        )

        // 2. Ordered reaction taps
        val allOtherInOrder = tappedNonBoosters + untappedNonBoosters
        allOtherInOrder.forEachIndexed { idx, player ->
            val rank = idx + 2
            val points = (n - rank + 1) * 10
            val awarded = if (points > 0) points else 10
            val tapTime = boostTapTimestamps[player.id]
            val reactionDelta = if (tapTime != null) (tapTime - startEpoch).coerceAtLeast(0L) else null
            val newTotal = player.totalScore + awarded

            orderedRanks.add(
                PlayerRoundRank(
                    rank = rank,
                    playerId = player.id,
                    playerName = player.name,
                    isBooster = false,
                    pointsAwarded = awarded,
                    cumulativeScore = newTotal,
                    reactionTimeMs = reactionDelta
                )
            )
        }

        // Update player model instances with their new scores
        for (i in players.indices) {
            val p = players[i]
            val rankInfo = orderedRanks.firstOrNull { it.playerId == p.id }
            if (rankInfo != null) {
                players[i] = p.copy(
                    totalScore = rankInfo.cumulativeScore,
                    roundScore = rankInfo.pointsAwarded
                )
            }
        }

        playState = playState.copy(
            roundResults = orderedRanks
        )

        // Broadcast ROUND_RESULTS
        val json = JSONObject().apply {
            put("type", NetworkProtocol.TYPE_ROUND_RESULTS)
            put("roundNumber", currentRound)
            put("boosterId", boosterId)
            put("boosterName", boosterP.name)
            val ranksArr = JSONArray()
            orderedRanks.forEach { r ->
                val rObj = JSONObject()
                rObj.put("rank", r.rank)
                rObj.put("playerId", r.playerId)
                rObj.put("playerName", r.playerName)
                rObj.put("isBooster", r.isBooster)
                rObj.put("pointsAwarded", r.pointsAwarded)
                rObj.put("cumulativeScore", r.cumulativeScore)
                rObj.put("reactionTimeMs", r.reactionTimeMs ?: -1L)
                ranksArr.put(rObj)
            }
            put("ranks", ranksArr)
        }
        broadcastRaw(json.toString())
        onStateUpdated(players.toList(), playState, "ROUND_RESULTS")
    }

    fun startNextRound() {
        currentRound++
        startWordSelectionPhase()
    }

    fun finishMatch() {
        currentStage = "WINNER"
        val topPlayer = players.maxByOrNull { it.totalScore } ?: players[0]
        playState = playState.copy(
            finalChampionName = topPlayer.name,
            finalChampionScore = topPlayer.totalScore
        )
        val json = JSONObject().apply {
            put("type", NetworkProtocol.TYPE_GAME_OVER)
            put("winnerName", topPlayer.name)
            put("winnerScore", topPlayer.totalScore)
        }
        broadcastRaw(json.toString())
        onStateUpdated(players.toList(), playState, "WINNER")
    }

    private fun advanceTurnTo(nextIdx: Int) {
        val targetIdx = (nextIdx + 1) % players.size
        playState = playState.copy(
            turnPlayerIndex = nextIdx,
            turnPlayerId = players[nextIdx].id,
            turnPlayerName = players[nextIdx].name,
            targetPlayerId = players[targetIdx].id,
            targetPlayerName = players[targetIdx].name
        )
        checkAndTriggerBotTurn()
    }

    private fun checkAndTriggerBotTurn() {
        botTurnJob?.cancel()
        val currentP = players.getOrNull(playState.turnPlayerIndex)
        if (currentP != null && currentP.isBot && currentStage == "IN_GAME" && playState.boosterPlayerId == null) {
            botTurnJob = scope.launch(Dispatchers.Default) {
                delay(1400)
                val botHand = playerHands[currentP.id] ?: return@launch
                if (botHand.isNotEmpty()) {
                    val counts = botHand.groupingBy { it }.eachCount()
                    val cardToPass = botHand.minByOrNull { counts[it] ?: 0 } ?: botHand.random()
                    executePassCard(currentP.id, cardToPass)
                }
            }
        }
    }

    fun restartGame() {
        currentRound = 1
        for (i in players.indices) {
            players[i] = players[i].copy(totalScore = 0, roundScore = 0)
        }
        startWordSelectionPhase()
    }

    fun returnToLobby() {
        currentStage = "LOBBY"
        currentRound = 1
        for (i in players.indices) {
            players[i] = players[i].copy(
                isReady = players[i].isHost || players[i].isBot,
                cards = emptyList(),
                submittedWord = "",
                hasSubmittedWord = false,
                totalScore = 0,
                roundScore = 0
            )
        }
        playerHands.clear()
        playState = GamePlayState()
        broadcastLobbyState()
    }

    private fun broadcastLobbyState() {
        val msg = NetworkProtocol.createLobbyUpdate(roomCode, roomName, players.toList())
        broadcastRaw(msg)
        onStateUpdated(players.toList(), playState, "LOBBY")
    }

    private fun broadcastWordSubmissions() {
        val json = JSONObject().apply {
            put("type", NetworkProtocol.TYPE_WORD_SUBMISSION_UPDATE)
            val arr = JSONArray()
            players.forEach { p ->
                val o = JSONObject()
                o.put("id", p.id)
                o.put("name", p.name)
                o.put("hasSubmitted", p.hasSubmittedWord)
                o.put("word", if (p.hasSubmittedWord) p.submittedWord else "")
                arr.put(o)
            }
            put("players", arr)
        }
        broadcastRaw(json.toString())
        onStateUpdated(players.toList(), playState, currentStage)
    }

    private fun broadcastGameState() {
        players.forEach { player ->
            val hand = playerHands[player.id]?.toList() ?: emptyList()
            val json = JSONObject().apply {
                put("type", NetworkProtocol.TYPE_GAME_STATE_UPDATE)
                put("roundNumber", playState.roundNumber)
                put("turnPlayerId", playState.turnPlayerId)
                put("turnPlayerName", playState.turnPlayerName)
                put("targetPlayerId", playState.targetPlayerId)
                put("targetPlayerName", playState.targetPlayerName)
                put("isFirstRotationComplete", playState.isFirstRotationComplete)
                put("totalMovesMade", playState.totalMovesMade)
                put("lastPassedCard", playState.lastPassedCard ?: "")
                put("lastPassedFrom", playState.lastPassedFromPlayer ?: "")
                put("lastPassedTo", playState.lastPassedToPlayer ?: "")
                put("boosterPlayerId", playState.boosterPlayerId ?: "")
                put("boosterPlayerName", playState.boosterPlayerName ?: "")
                put("winningWord", playState.winningWord ?: "")

                val cardsArr = JSONArray()
                hand.forEach { cardsArr.put(it) }
                put("yourCards", cardsArr)

                val playersArr = JSONArray()
                players.forEach { p ->
                    val pObj = JSONObject()
                    pObj.put("id", p.id)
                    pObj.put("name", p.name)
                    pObj.put("isHost", p.isHost)
                    pObj.put("isBot", p.isBot)
                    pObj.put("cardCount", playerHands[p.id]?.size ?: 0)
                    pObj.put("pingMs", p.pingMs)
                    pObj.put("totalScore", p.totalScore)
                    playersArr.put(pObj)
                }
                put("players", playersArr)
            }

            if (player.id == hostPlayer.id) {
                val updatedPlayers = players.map { p ->
                    if (p.id == hostPlayer.id) p.copy(cards = hand, cardCount = hand.size)
                    else p.copy(cards = emptyList(), cardCount = playerHands[p.id]?.size ?: 0)
                }
                onStateUpdated(updatedPlayers, playState, currentStage)
            } else {
                clientWriters[player.id]?.println(json.toString())
            }
        }
    }

    private fun broadcastRaw(message: String) {
        clientWriters.values.forEach { writer ->
            try {
                writer.println(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error writing to client", e)
            }
        }
    }

    fun stop() {
        try {
            serverJob?.cancel()
            botTurnJob?.cancel()
            boostTimerJob?.cancel()
            botBoostTapsJob?.cancel()
            clientWriters.values.forEach { it.close() }
            clientWriters.clear()
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server", e)
        }
    }
}
