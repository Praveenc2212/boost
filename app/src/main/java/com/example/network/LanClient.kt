package com.example.network

import android.util.Log
import com.example.model.BoostReactionState
import com.example.model.GamePlayState
import com.example.model.Player
import com.example.model.PlayerRoundRank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class LanClient(
    private val scope: CoroutineScope,
    val localPlayer: Player,
    private val onLobbyUpdated: (String, String, List<Player>) -> Unit,
    private val onWordSelectionStarted: () -> Unit,
    private val onWordSubmissionsUpdated: (List<Player>) -> Unit,
    private val onShuffleStarted: () -> Unit,
    private val onGameStateUpdated: (List<Player>, GamePlayState, List<String>) -> Unit,
    private val onBoostStarted: (BoostReactionState) -> Unit,
    private val onBoostReactionUpdated: (List<String>) -> Unit,
    private val onRoundResults: (List<PlayerRoundRank>, Int, String, String) -> Unit,
    private val onGameOver: (String, Int) -> Unit,
    private val onDisconnected: (String) -> Unit
) {
    private val TAG = "LanClient"
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var listenJob: Job? = null

    fun connect(hostIp: String, port: Int = NetworkProtocol.TCP_GAME_PORT) {
        listenJob = scope.launch(Dispatchers.IO) {
            try {
                val s = Socket()
                s.connect(InetSocketAddress(hostIp, port), 5000)
                socket = s

                writer = PrintWriter(s.getOutputStream().bufferedWriter(Charsets.UTF_8), true)
                val reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))

                // Send Join Request
                val joinMsg = NetworkProtocol.createJoinRequest(localPlayer.id, localPlayer.name)
                writer?.println(joinMsg)

                while (isActive) {
                    val line = reader.readLine() ?: break
                    handleServerMessage(line)
                }
            } catch (e: Exception) {
                Log.e(TAG, "LanClient connection error", e)
                onDisconnected(e.localizedMessage ?: "Disconnected from host")
            } finally {
                disconnect()
            }
        }
    }

    private fun handleServerMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.optString("type")

            when (type) {
                NetworkProtocol.TYPE_LOBBY_UPDATE -> {
                    val roomCode = json.getString("roomCode")
                    val roomName = json.getString("roomName")
                    val arr = json.getJSONArray("players")
                    val playersList = mutableListOf<Player>()
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        playersList.add(
                            Player(
                                id = p.getString("id"),
                                name = p.getString("name"),
                                isHost = p.optBoolean("isHost", false),
                                isReady = p.optBoolean("isReady", false),
                                isBot = p.optBoolean("isBot", false),
                                pingMs = p.optLong("pingMs", 12L),
                                totalScore = p.optInt("totalScore", 0)
                            )
                        )
                    }
                    onLobbyUpdated(roomCode, roomName, playersList)
                }

                NetworkProtocol.TYPE_START_WORD_SELECTION -> {
                    onWordSelectionStarted()
                }

                NetworkProtocol.TYPE_WORD_SUBMISSION_UPDATE -> {
                    val arr = json.getJSONArray("players")
                    val playersList = mutableListOf<Player>()
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        playersList.add(
                            Player(
                                id = p.getString("id"),
                                name = p.getString("name"),
                                submittedWord = p.optString("word", ""),
                                hasSubmittedWord = p.optBoolean("hasSubmitted", false)
                            )
                        )
                    }
                    onWordSubmissionsUpdated(playersList)
                }

                NetworkProtocol.TYPE_SHUFFLE_START -> {
                    onShuffleStarted()
                }

                NetworkProtocol.TYPE_GAME_STATE_UPDATE -> {
                    val round = json.getInt("roundNumber")
                    val turnPlayerId = json.getString("turnPlayerId")
                    val turnPlayerName = json.getString("turnPlayerName")
                    val targetPlayerId = json.getString("targetPlayerId")
                    val targetPlayerName = json.getString("targetPlayerName")
                    val rotationComplete = json.optBoolean("isFirstRotationComplete", false)
                    val totalMoves = json.optInt("totalMovesMade", 0)
                    val lastCard = json.optString("lastPassedCard", "")
                    val lastFrom = json.optString("lastPassedFrom", "")
                    val lastTo = json.optString("lastPassedTo", "")
                    val boosterId = json.optString("boosterPlayerId", "")
                    val boosterName = json.optString("boosterPlayerName", "")
                    val winningWord = json.optString("winningWord", "")

                    // Your cards
                    val yourCardsArr = json.getJSONArray("yourCards")
                    val yourCards = mutableListOf<String>()
                    for (i in 0 until yourCardsArr.length()) {
                        yourCards.add(yourCardsArr.getString(i))
                    }

                    // All players
                    val playersArr = json.getJSONArray("players")
                    val playersList = mutableListOf<Player>()
                    for (i in 0 until playersArr.length()) {
                        val p = playersArr.getJSONObject(i)
                        val pid = p.getString("id")
                        playersList.add(
                            Player(
                                id = pid,
                                name = p.getString("name"),
                                isHost = p.optBoolean("isHost", false),
                                isBot = p.optBoolean("isBot", false),
                                cards = if (pid == localPlayer.id) yourCards else emptyList(),
                                cardCount = p.optInt("cardCount", 0),
                                pingMs = p.optLong("pingMs", 15L),
                                totalScore = p.optInt("totalScore", 0)
                            )
                        )
                    }

                    val playState = GamePlayState(
                        roundNumber = round,
                        turnPlayerId = turnPlayerId,
                        turnPlayerName = turnPlayerName,
                        targetPlayerId = targetPlayerId,
                        targetPlayerName = targetPlayerName,
                        isFirstRotationComplete = rotationComplete,
                        totalMovesMade = totalMoves,
                        lastPassedCard = lastCard.takeIf { it.isNotEmpty() },
                        lastPassedFromPlayer = lastFrom.takeIf { it.isNotEmpty() },
                        lastPassedToPlayer = lastTo.takeIf { it.isNotEmpty() },
                        boosterPlayerId = boosterId.takeIf { it.isNotEmpty() },
                        boosterPlayerName = boosterName.takeIf { it.isNotEmpty() },
                        winningWord = winningWord.takeIf { it.isNotEmpty() }
                    )

                    onGameStateUpdated(playersList, playState, yourCards)
                }

                NetworkProtocol.TYPE_BOOST_STARTED -> {
                    val boosterId = json.getString("boosterId")
                    val boosterName = json.getString("boosterName")
                    val winWord = json.getString("winningWord")
                    val startMs = json.optLong("startTimeMs", System.currentTimeMillis())
                    val dur = json.optDouble("durationSeconds", 3.8).toFloat()

                    val reactionState = BoostReactionState(
                        boosterPlayerId = boosterId,
                        boosterPlayerName = boosterName,
                        winningWord = winWord,
                        startTimeEpochMs = startMs,
                        durationSeconds = dur,
                        tappedPlayerIds = listOf(boosterId)
                    )
                    onBoostStarted(reactionState)
                }

                NetworkProtocol.TYPE_BOOST_REACTION_UPDATE -> {
                    val arr = json.getJSONArray("tappedPlayerIds")
                    val tappedList = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        tappedList.add(arr.getString(i))
                    }
                    onBoostReactionUpdated(tappedList)
                }

                NetworkProtocol.TYPE_ROUND_RESULTS -> {
                    val roundNum = json.optInt("roundNumber", 1)
                    val boosterId = json.getString("boosterId")
                    val boosterName = json.getString("boosterName")
                    val ranksArr = json.getJSONArray("ranks")
                    val ranksList = mutableListOf<PlayerRoundRank>()
                    for (i in 0 until ranksArr.length()) {
                        val rObj = ranksArr.getJSONObject(i)
                        val reactMs = rObj.optLong("reactionTimeMs", -1L)
                        ranksList.add(
                            PlayerRoundRank(
                                rank = rObj.getInt("rank"),
                                playerId = rObj.getString("playerId"),
                                playerName = rObj.getString("playerName"),
                                isBooster = rObj.getBoolean("isBooster"),
                                pointsAwarded = rObj.getInt("pointsAwarded"),
                                cumulativeScore = rObj.getInt("cumulativeScore"),
                                reactionTimeMs = if (reactMs >= 0) reactMs else null
                            )
                        )
                    }
                    onRoundResults(ranksList, roundNum, boosterId, boosterName)
                }

                NetworkProtocol.TYPE_GAME_OVER -> {
                    val winnerName = json.getString("winnerName")
                    val winnerScore = json.optInt("winnerScore", 0)
                    onGameOver(winnerName, winnerScore)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling server message", e)
        }
    }

    fun toggleReady(isReady: Boolean) {
        val msg = NetworkProtocol.createToggleReady(localPlayer.id, isReady)
        send(msg)
    }

    fun submitWord(word: String) {
        val msg = NetworkProtocol.createSubmitWord(localPlayer.id, word)
        send(msg)
    }

    fun passCard(cardWord: String) {
        val msg = NetworkProtocol.createPassCard(localPlayer.id, cardWord)
        send(msg)
    }

    fun triggerBoost() {
        val msg = NetworkProtocol.createTriggerBoost(localPlayer.id)
        send(msg)
    }

    fun sendBoostTap(timestampMs: Long = System.currentTimeMillis()) {
        val msg = NetworkProtocol.createBoostTap(localPlayer.id, timestampMs)
        send(msg)
    }

    fun requestNextRound() {
        val msg = NetworkProtocol.createNextRound(localPlayer.id)
        send(msg)
    }

    fun leave() {
        val json = JSONObject().apply {
            put("type", NetworkProtocol.TYPE_LEAVE)
            put("playerId", localPlayer.id)
        }
        send(json.toString())
        disconnect()
    }

    private fun send(message: String) {
        scope.launch(Dispatchers.IO) {
            try {
                writer?.println(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }

    fun disconnect() {
        try {
            listenJob?.cancel()
            writer?.close()
            socket?.close()
            socket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during client disconnect", e)
        }
    }
}

