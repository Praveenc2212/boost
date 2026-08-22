package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.model.GameStage
import com.example.ui.components.DeckShuffleAnimation
import com.example.ui.components.ParticleBackground
import com.example.ui.screens.BoostActivatedScreen
import com.example.ui.screens.BoostAvailableScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InGameScreen
import com.example.ui.screens.LanSearchScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.RoundResultsScreen
import com.example.ui.screens.WinnerScreen
import com.example.ui.screens.WordSelectionScreen
import com.example.ui.theme.BoostTheme
import com.example.viewmodel.BoostGameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BoostGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoostTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.statusMessage) {
                    uiState.statusMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Ambient particle background
                        ParticleBackground()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .safeDrawingPadding()
                        ) {
                            AnimatedContent(
                                targetState = uiState.stage,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "stage_anim"
                            ) { stage ->
                                when (stage) {
                                    GameStage.HOME -> {
                                        HomeScreen(
                                            localPlayer = uiState.localPlayer,
                                            onPlayerNameChange = { viewModel.updatePlayerName(it) },
                                            onCreateRoom = { viewModel.startCreateRoom() },
                                            onJoinRoom = { viewModel.startLanSearch() },
                                            onQuickSoloMatch = { viewModel.startQuickSoloMatch() }
                                        )
                                    }

                                    GameStage.LAN_SEARCH -> {
                                        LanSearchScreen(
                                            discoveredRooms = uiState.discoveredRooms,
                                            onJoinRoom = { viewModel.joinRoom(it) },
                                            onDirectIpJoin = { viewModel.joinByIpDirect(it) },
                                            onBack = { viewModel.goToHome() }
                                        )
                                    }

                                    GameStage.LOBBY, GameStage.WAITING_FOR_PLAYERS -> {
                                        LobbyScreen(
                                            roomName = uiState.roomName,
                                            roomCode = uiState.roomCode,
                                            players = uiState.players,
                                            localPlayer = uiState.localPlayer,
                                            isHost = uiState.isHost,
                                            onToggleReady = { viewModel.toggleReady() },
                                            onStartGame = { viewModel.hostStartGame() },
                                            onAddBot = { viewModel.addBot() },
                                            onRemovePlayer = { viewModel.removePlayer(it) },
                                            onLeaveRoom = { viewModel.goToHome() }
                                        )
                                    }

                                    GameStage.WORD_SELECTION -> {
                                        WordSelectionScreen(
                                            wordInput = uiState.wordInput,
                                            validationError = uiState.wordValidationError,
                                            isSubmitted = uiState.isWordSubmitted,
                                            players = uiState.players,
                                            localPlayer = uiState.localPlayer,
                                            onWordChanged = { viewModel.onWordInputChanged(it) },
                                            onWordChipSelected = { viewModel.selectWordChip(it) },
                                            onSubmitWord = { viewModel.submitWord() }
                                        )
                                    }

                                    GameStage.SHUFFLE_ANIMATION -> {
                                        DeckShuffleAnimation()
                                    }

                                    GameStage.IN_GAME -> {
                                        InGameScreen(
                                            players = uiState.players,
                                            localPlayer = uiState.localPlayer,
                                            playState = uiState.playState,
                                            myCards = uiState.myCards,
                                            selectedCardIndex = uiState.selectedCardIndex,
                                            onSelectCard = { viewModel.selectCard(it) },
                                            onPassCard = { viewModel.passSelectedCard() },
                                            onLeaveGame = { viewModel.goToHome() }
                                        )
                                    }

                                    GameStage.BOOST_AVAILABLE -> {
                                        BoostAvailableScreen(
                                            winningWord = uiState.playState.winningWord ?: "CARDS",
                                            myCards = uiState.myCards,
                                            onTriggerBoost = { viewModel.triggerBoost() }
                                        )
                                    }

                                    GameStage.BOOST_ACTIVATED -> {
                                        BoostActivatedScreen(
                                            players = uiState.players,
                                            localPlayer = uiState.localPlayer,
                                            boostReaction = uiState.playState.boostReaction,
                                            hasTapped = uiState.hasTappedReactionBoost,
                                            onTapBoost = { viewModel.tapBoostReaction() }
                                        )
                                    }

                                    GameStage.ROUND_RESULTS -> {
                                        RoundResultsScreen(
                                            roundNumber = uiState.playState.roundNumber,
                                            boosterName = uiState.playState.boosterPlayerName ?: "Booster",
                                            results = uiState.playState.roundResults,
                                            localPlayer = uiState.localPlayer,
                                            isHost = uiState.isHost,
                                            onNextRound = { viewModel.nextRound() },
                                            onFinishMatch = { viewModel.finishMatch() },
                                            onReturnToLobby = { viewModel.returnToLobby() }
                                        )
                                    }

                                    GameStage.WINNER -> {
                                        WinnerScreen(
                                            winnerName = uiState.playState.finalChampionName ?: uiState.playState.boosterPlayerName ?: "Champion",
                                            winnerScore = uiState.playState.finalChampionScore,
                                            winningWord = uiState.playState.winningWord ?: "MATCH",
                                            players = uiState.players,
                                            isHost = uiState.isHost,
                                            onPlayAgain = { viewModel.playAgain() },
                                            onReturnToLobby = { viewModel.returnToLobby() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


