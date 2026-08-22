package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DiscoveredRoom
import com.example.model.GamePlayState
import com.example.model.GameStage
import com.example.model.Player
import com.example.network.GameEngine
import com.example.network.LanClient
import com.example.network.LanDiscoveryManager
import com.example.network.LanServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class BoostUiState(
    val stage: GameStage = GameStage.HOME,
    val localPlayer: Player = Player(
        id = UUID.randomUUID().toString().take(8),
        name = "Player"
    ),
    val isHost: Boolean = false,
    val roomCode: String = "",
    val roomName: String = "",
    val players: List<Player> = emptyList(),
    val discoveredRooms: List<DiscoveredRoom> = emptyList(),
    val playState: GamePlayState = GamePlayState(),
    val myCards: List<String> = emptyList(),
    val selectedCardIndex: Int? = null,
    val wordInput: String = "",
    val wordValidationError: String? = null,
    val isWordSubmitted: Boolean = false,
    val isShuffling: Boolean = false,
    val statusMessage: String? = null,
    val isPassingCard: Boolean = false,
    val hasTappedReactionBoost: Boolean = false
)

class BoostGameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BoostUiState())
    val uiState = _uiState.asStateFlow()

    private val discoveryManager = LanDiscoveryManager(application)
    private var lanServer: LanServer? = null
    private var lanClient: LanClient? = null

    private var shuffleJob: Job? = null

    init {
        // Load or initialize default player name
        val prefs = application.getSharedPreferences("boost_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("player_name", null) ?: "Player_${(100..999).random()}"
        val playerId = prefs.getString("player_id", null) ?: "P_${UUID.randomUUID().toString().take(6)}"

        prefs.edit().putString("player_name", savedName).putString("player_id", playerId).apply()

        _uiState.update {
            it.copy(
                localPlayer = Player(id = playerId, name = savedName)
            )
        }

        // Collect discovered rooms
        viewModelScope.launch {
            discoveryManager.discoveredRooms.collect { rooms ->
                _uiState.update { it.copy(discoveredRooms = rooms) }
            }
        }
    }

    fun updatePlayerName(newName: String) {
        val trimmed = newName.trim().take(14)
        if (trimmed.isNotEmpty()) {
            getApplication<Application>().getSharedPreferences("boost_prefs", Context.MODE_PRIVATE)
                .edit().putString("player_name", trimmed).apply()
            _uiState.update { it.copy(localPlayer = it.localPlayer.copy(name = trimmed)) }
        }
    }

    fun goToHome() {
        cleanupNetwork()
        _uiState.update {
            it.copy(
                stage = GameStage.HOME,
                isHost = false,
                roomCode = "",
                roomName = "",
                players = emptyList(),
                myCards = emptyList(),
                selectedCardIndex = null,
                wordInput = "",
                isWordSubmitted = false,
                isShuffling = false,
                statusMessage = null,
                hasTappedReactionBoost = false,
                playState = GamePlayState()
            )
        }
    }

    fun startCreateRoom(customRoomName: String? = null) {
        cleanupNetwork()
        val p = _uiState.value.localPlayer.copy(isHost = true, isReady = true)
        val roomCode = "BOOST-${(1000..9999).random()}"
        val roomName = customRoomName?.takeIf { it.isNotBlank() } ?: "${p.name}'s Arena"

        val server = LanServer(
            scope = viewModelScope,
            roomCode = roomCode,
            roomName = roomName,
            hostPlayer = p,
            onStateUpdated = { updatedPlayers, updatedPlayState, stageTag ->
                handleServerStateUpdate(updatedPlayers, updatedPlayState, stageTag)
            }
        )
        lanServer = server
        server.start()

        // Start broadcasting discovery beacon
        discoveryManager.startHostingBroadcast(
            scope = viewModelScope,
            roomCode = roomCode,
            roomName = roomName,
            hostName = p.name,
            playerCountProvider = { server.players.size }
        )

        _uiState.update {
            it.copy(
                stage = GameStage.LOBBY,
                isHost = true,
                roomCode = roomCode,
                roomName = roomName,
                players = listOf(p),
                localPlayer = p
            )
        }
    }

    fun startQuickSoloMatch() {
        startCreateRoom("Cyber Arena")
        viewModelScope.launch {
            delay(200)
            lanServer?.addBotPlayer()
            delay(100)
            lanServer?.addBotPlayer()
            delay(100)
            lanServer?.addBotPlayer()
        }
    }

    fun startLanSearch() {
        cleanupNetwork()
        _uiState.update { it.copy(stage = GameStage.LAN_SEARCH) }
        discoveryManager.startScanning(viewModelScope)
    }

    fun joinRoom(room: DiscoveredRoom) {
        discoveryManager.stopScanning()
        val p = _uiState.value.localPlayer.copy(isHost = false, isReady = false)

        val client = LanClient(
            scope = viewModelScope,
            localPlayer = p,
            onLobbyUpdated = { roomCode, roomName, playersList ->
                _uiState.update {
                    it.copy(
                        stage = GameStage.LOBBY,
                        roomCode = roomCode,
                        roomName = roomName,
                        players = playersList,
                        localPlayer = playersList.firstOrNull { pl -> pl.id == p.id } ?: p
                    )
                }
            },
            onWordSelectionStarted = {
                _uiState.update {
                    it.copy(
                        stage = GameStage.WORD_SELECTION,
                        wordInput = "",
                        isWordSubmitted = false,
                        wordValidationError = null
                    )
                }
            },
            onWordSubmissionsUpdated = { playersList ->
                _uiState.update { current ->
                    val updated = current.players.map { existing ->
                        val remote = playersList.firstOrNull { it.id == existing.id }
                        if (remote != null) existing.copy(
                            hasSubmittedWord = remote.hasSubmittedWord,
                            submittedWord = remote.submittedWord
                        )
                        else existing
                    }
                    current.copy(players = updated)
                }
            },
            onShuffleStarted = {
                triggerShuffleAnimation()
            },
            onGameStateUpdated = { playersList, playState, yourCards ->
                val isBooster = playState.boosterPlayerId == p.id
                val nextStage = if (isBooster) GameStage.BOOST_AVAILABLE else GameStage.IN_GAME
                _uiState.update {
                    it.copy(
                        stage = nextStage,
                        players = playersList,
                        playState = playState,
                        myCards = yourCards,
                        isShuffling = false,
                        isPassingCard = false,
                        hasTappedReactionBoost = false
                    )
                }
            },
            onBoostStarted = { reactionState ->
                _uiState.update {
                    it.copy(
                        stage = GameStage.BOOST_ACTIVATED,
                        playState = it.playState.copy(
                            boosterPlayerId = reactionState.boosterPlayerId,
                            boosterPlayerName = reactionState.boosterPlayerName,
                            winningWord = reactionState.winningWord,
                            boostReaction = reactionState
                        ),
                        hasTappedReactionBoost = reactionState.boosterPlayerId == p.id
                    )
                }
            },
            onBoostReactionUpdated = { tappedList ->
                _uiState.update { current ->
                    current.copy(
                        playState = current.playState.copy(
                            boostReaction = current.playState.boostReaction?.copy(tappedPlayerIds = tappedList)
                        ),
                        hasTappedReactionBoost = current.hasTappedReactionBoost || (p.id in tappedList)
                    )
                }
            },
            onRoundResults = { ranksList, roundNum, boosterId, boosterName ->
                _uiState.update { current ->
                    val updatedPlayers = current.players.map { player ->
                        val r = ranksList.firstOrNull { it.playerId == player.id }
                        if (r != null) player.copy(totalScore = r.cumulativeScore, roundScore = r.pointsAwarded)
                        else player
                    }
                    current.copy(
                        stage = GameStage.ROUND_RESULTS,
                        players = updatedPlayers,
                        playState = current.playState.copy(
                            roundNumber = roundNum,
                            boosterPlayerId = boosterId,
                            boosterPlayerName = boosterName,
                            roundResults = ranksList
                        )
                    )
                }
            },
            onGameOver = { winnerName, winnerScore ->
                _uiState.update {
                    it.copy(
                        stage = GameStage.WINNER,
                        playState = it.playState.copy(
                            finalChampionName = winnerName,
                            finalChampionScore = winnerScore
                        )
                    )
                }
            },
            onDisconnected = { reason ->
                _uiState.update {
                    it.copy(
                        statusMessage = "Disconnected: $reason"
                    )
                }
            }
        )
        lanClient = client
        client.connect(room.hostIp, room.port)

        _uiState.update {
            it.copy(
                isHost = false,
                roomCode = room.roomCode,
                roomName = room.roomName
            )
        }
    }

    fun joinByIpDirect(hostIp: String, port: Int = 38291) {
        val dummyRoom = DiscoveredRoom(
            roomCode = "BOOST-DIRECT",
            roomName = "Direct Game",
            hostName = "Host",
            hostIp = hostIp.trim(),
            port = port
        )
        joinRoom(dummyRoom)
    }

    fun toggleReady() {
        val currentReady = _uiState.value.localPlayer.isReady
        val newReady = !currentReady
        _uiState.update {
            it.copy(localPlayer = it.localPlayer.copy(isReady = newReady))
        }
        if (!_uiState.value.isHost) {
            lanClient?.toggleReady(newReady)
        }
    }

    fun addBot() {
        if (_uiState.value.isHost) {
            lanServer?.addBotPlayer()
        }
    }

    fun removePlayer(playerId: String) {
        if (_uiState.value.isHost) {
            lanServer?.removePlayer(playerId)
        }
    }

    fun hostStartGame() {
        if (_uiState.value.isHost) {
            lanServer?.startWordSelectionPhase()
            _uiState.update {
                it.copy(
                    stage = GameStage.WORD_SELECTION,
                    wordInput = "",
                    isWordSubmitted = false,
                    wordValidationError = null
                )
            }
        }
    }

    fun onWordInputChanged(text: String) {
        val filtered = text.uppercase().filter { it in 'A'..'Z' }.take(12)
        val existingWords = _uiState.value.players
            .filter { it.id != _uiState.value.localPlayer.id && it.submittedWord.isNotEmpty() }
            .map { it.submittedWord }
        val (isValid, errorMsg) = GameEngine.validateWord(filtered, existingWords)

        _uiState.update {
            it.copy(
                wordInput = filtered,
                wordValidationError = if (filtered.isNotEmpty() && !isValid) errorMsg else null
            )
        }
    }

    fun selectWordChip(word: String) {
        onWordInputChanged(word)
    }

    fun submitWord() {
        val word = _uiState.value.wordInput.trim().uppercase()
        val existingWords = _uiState.value.players
            .filter { it.id != _uiState.value.localPlayer.id && it.submittedWord.isNotEmpty() }
            .map { it.submittedWord }
        val (isValid, errorMsg) = GameEngine.validateWord(word, existingWords)

        if (!isValid) {
            _uiState.update { it.copy(wordValidationError = errorMsg) }
            return
        }

        _uiState.update {
            it.copy(
                isWordSubmitted = true,
                wordValidationError = null
            )
        }

        if (_uiState.value.isHost) {
            lanServer?.hostSubmitWord(word)
        } else {
            lanClient?.submitWord(word)
        }
    }

    private fun triggerShuffleAnimation() {
        shuffleJob?.cancel()
        _uiState.update { it.copy(stage = GameStage.SHUFFLE_ANIMATION, isShuffling = true) }
    }

    private fun handleServerStateUpdate(
        updatedPlayers: List<Player>,
        updatedPlayState: GamePlayState,
        stageTag: String?
    ) {
        val localId = _uiState.value.localPlayer.id
        val myCards = updatedPlayers.firstOrNull { it.id == localId }?.cards ?: emptyList()

        val nextStage = when (stageTag) {
            "SHUFFLE" -> GameStage.SHUFFLE_ANIMATION
            "BOOST_ACTIVATED" -> GameStage.BOOST_ACTIVATED
            "ROUND_RESULTS" -> GameStage.ROUND_RESULTS
            "WINNER" -> GameStage.WINNER
            "LOBBY" -> GameStage.LOBBY
            "WORD_SELECTION" -> GameStage.WORD_SELECTION
            else -> {
                if (updatedPlayState.boosterPlayerId == localId) GameStage.BOOST_AVAILABLE
                else if (updatedPlayState.turnPlayerId.isNotEmpty()) GameStage.IN_GAME
                else _uiState.value.stage
            }
        }

        _uiState.update {
            it.copy(
                stage = nextStage,
                players = updatedPlayers,
                playState = updatedPlayState,
                myCards = myCards,
                isShuffling = stageTag == "SHUFFLE",
                isPassingCard = false
            )
        }
    }

    fun selectCard(index: Int) {
        _uiState.update {
            val nextIndex = if (it.selectedCardIndex == index) null else index
            it.copy(selectedCardIndex = nextIndex)
        }
    }

    fun passSelectedCard() {
        val state = _uiState.value
        val cardIdx = state.selectedCardIndex ?: return
        val cardWord = state.myCards.getOrNull(cardIdx) ?: return

        // Verify it is currently our turn
        if (state.playState.turnPlayerId != state.localPlayer.id) {
            return
        }

        _uiState.update { it.copy(isPassingCard = true, selectedCardIndex = null) }

        if (state.isHost) {
            lanServer?.hostPassCard(cardWord)
        } else {
            lanClient?.passCard(cardWord)
        }
    }

    fun triggerBoost() {
        if (_uiState.value.isHost) {
            lanServer?.hostTriggerBoost()
        } else {
            lanClient?.triggerBoost()
        }
    }

    fun tapBoostReaction() {
        if (_uiState.value.hasTappedReactionBoost) return
        _uiState.update { it.copy(hasTappedReactionBoost = true) }

        val timestamp = System.currentTimeMillis()
        if (_uiState.value.isHost) {
            lanServer?.hostBoostTap(timestamp)
        } else {
            lanClient?.sendBoostTap(timestamp)
        }
    }

    fun nextRound() {
        if (_uiState.value.isHost) {
            lanServer?.startNextRound()
        } else {
            lanClient?.requestNextRound()
        }
    }

    fun finishMatch() {
        if (_uiState.value.isHost) {
            lanServer?.finishMatch()
        }
    }

    fun playAgain() {
        if (_uiState.value.isHost) {
            lanServer?.restartGame()
        }
        _uiState.update {
            it.copy(
                wordInput = "",
                isWordSubmitted = false,
                selectedCardIndex = null,
                myCards = emptyList(),
                hasTappedReactionBoost = false
            )
        }
    }

    fun returnToLobby() {
        if (_uiState.value.isHost) {
            lanServer?.returnToLobby()
        }
        _uiState.update {
            it.copy(
                stage = GameStage.LOBBY,
                wordInput = "",
                isWordSubmitted = false,
                selectedCardIndex = null,
                myCards = emptyList(),
                hasTappedReactionBoost = false
            )
        }
    }

    private fun cleanupNetwork() {
        discoveryManager.stopHostingBroadcast()
        discoveryManager.stopScanning()
        lanServer?.stop()
        lanServer = null
        lanClient?.disconnect()
        lanClient = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanupNetwork()
    }
}
