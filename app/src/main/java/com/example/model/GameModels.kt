package com.example.model

enum class GameStage {
    HOME,
    LAN_SEARCH,
    LOBBY,
    WORD_SELECTION,
    WAITING_FOR_PLAYERS,
    SHUFFLE_ANIMATION,
    IN_GAME,
    BOOST_AVAILABLE,
    BOOST_ACTIVATED,
    ROUND_RESULTS,
    WINNER
}

data class Player(
    val id: String,
    val name: String,
    val isHost: Boolean = false,
    val isReady: Boolean = false,
    val cards: List<String> = emptyList(),
    val cardCount: Int = cards.size,
    val submittedWord: String = "",
    val hasSubmittedWord: Boolean = submittedWord.isNotEmpty(),
    val isBot: Boolean = false,
    val pingMs: Long = 12L,
    val totalScore: Int = 0,
    val roundScore: Int = 0,
    val hasPressedBoost: Boolean = false,
    val boostPressTimeMs: Long? = null
)

data class DiscoveredRoom(
    val roomCode: String,
    val roomName: String,
    val hostName: String,
    val hostIp: String,
    val port: Int = 38291,
    val currentPlayers: Int = 1,
    val maxPlayers: Int = 6,
    val pingMs: Long = 15L,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

data class PlayerRoundRank(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val isBooster: Boolean,
    val pointsAwarded: Int,
    val cumulativeScore: Int,
    val reactionTimeMs: Long? = null
)

data class BoostReactionState(
    val boosterPlayerId: String = "",
    val boosterPlayerName: String = "",
    val winningWord: String = "",
    val startTimeEpochMs: Long = 0L,
    val durationSeconds: Float = 3.8f,
    val tappedPlayerIds: List<String> = emptyList()
)

data class GamePlayState(
    val roundNumber: Int = 1,
    val turnPlayerIndex: Int = 0,
    val turnPlayerId: String = "",
    val turnPlayerName: String = "",
    val targetPlayerId: String = "",
    val targetPlayerName: String = "",
    val isFirstRotationComplete: Boolean = false,
    val totalMovesMade: Int = 0,
    val lastPassedCard: String? = null,
    val lastPassedFromPlayer: String? = null,
    val lastPassedToPlayer: String? = null,
    val boosterPlayerId: String? = null,
    val boosterPlayerName: String? = null,
    val winningWord: String? = null,
    val boostReaction: BoostReactionState? = null,
    val roundResults: List<PlayerRoundRank> = emptyList(),
    val finalChampionName: String? = null,
    val finalChampionScore: Int = 0
)

