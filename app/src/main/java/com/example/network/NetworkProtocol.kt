package com.example.network

import com.example.model.BoostReactionState
import com.example.model.Player
import com.example.model.PlayerRoundRank
import org.json.JSONArray
import org.json.JSONObject

object NetworkProtocol {
    const val UDP_DISCOVERY_PORT = 38290
    const val TCP_GAME_PORT = 38291

    // Message Types
    const val TYPE_BEACON = "BEACON"
    const val TYPE_JOIN_REQUEST = "JOIN_REQUEST"
    const val TYPE_JOIN_RESPONSE = "JOIN_RESPONSE"
    const val TYPE_LOBBY_UPDATE = "LOBBY_UPDATE"
    const val TYPE_START_WORD_SELECTION = "START_WORD_SELECTION"
    const val TYPE_SUBMIT_WORD = "SUBMIT_WORD"
    const val TYPE_WORD_SUBMISSION_UPDATE = "WORD_SUBMISSION_UPDATE"
    const val TYPE_SHUFFLE_START = "SHUFFLE_START"
    const val TYPE_START_GAME = "START_GAME"
    const val TYPE_PASS_CARD = "PASS_CARD"
    const val TYPE_GAME_STATE_UPDATE = "GAME_STATE_UPDATE"
    const val TYPE_TRIGGER_BOOST = "TRIGGER_BOOST"
    const val TYPE_BOOST_STARTED = "BOOST_STARTED"
    const val TYPE_BOOST_TAP = "BOOST_TAP"
    const val TYPE_BOOST_REACTION_UPDATE = "BOOST_REACTION_UPDATE"
    const val TYPE_ROUND_RESULTS = "ROUND_RESULTS"
    const val TYPE_NEXT_ROUND = "NEXT_ROUND"
    const val TYPE_GAME_OVER = "GAME_OVER"
    const val TYPE_LEAVE = "LEAVE"
    const val TYPE_PLAY_AGAIN = "PLAY_AGAIN"
    const val TYPE_TOGGLE_READY = "TOGGLE_READY"

    fun createBeacon(
        roomCode: String,
        roomName: String,
        hostName: String,
        currentPlayers: Int,
        maxPlayers: Int = 6,
        port: Int = TCP_GAME_PORT
    ): String {
        val json = JSONObject()
        json.put("type", TYPE_BEACON)
        json.put("roomCode", roomCode)
        json.put("roomName", roomName)
        json.put("hostName", hostName)
        json.put("currentPlayers", currentPlayers)
        json.put("maxPlayers", maxPlayers)
        json.put("port", port)
        return json.toString()
    }

    fun parseBeacon(jsonString: String, senderIp: String): com.example.model.DiscoveredRoom? {
        return try {
            val json = JSONObject(jsonString)
            if (json.optString("type") == TYPE_BEACON) {
                com.example.model.DiscoveredRoom(
                    roomCode = json.getString("roomCode"),
                    roomName = json.getString("roomName"),
                    hostName = json.getString("hostName"),
                    hostIp = senderIp,
                    port = json.optInt("port", TCP_GAME_PORT),
                    currentPlayers = json.optInt("currentPlayers", 1),
                    maxPlayers = json.optInt("maxPlayers", 6),
                    pingMs = (8..24).random().toLong(),
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun createJoinRequest(playerId: String, playerName: String): String {
        val json = JSONObject()
        json.put("type", TYPE_JOIN_REQUEST)
        json.put("playerId", playerId)
        json.put("playerName", playerName)
        return json.toString()
    }

    fun createToggleReady(playerId: String, isReady: Boolean): String {
        val json = JSONObject()
        json.put("type", TYPE_TOGGLE_READY)
        json.put("playerId", playerId)
        json.put("isReady", isReady)
        return json.toString()
    }

    fun createSubmitWord(playerId: String, word: String): String {
        val json = JSONObject()
        json.put("type", TYPE_SUBMIT_WORD)
        json.put("playerId", playerId)
        json.put("word", word)
        return json.toString()
    }

    fun createPassCard(playerId: String, cardWord: String): String {
        val json = JSONObject()
        json.put("type", TYPE_PASS_CARD)
        json.put("playerId", playerId)
        json.put("cardWord", cardWord)
        return json.toString()
    }

    fun createTriggerBoost(playerId: String): String {
        val json = JSONObject()
        json.put("type", TYPE_TRIGGER_BOOST)
        json.put("playerId", playerId)
        return json.toString()
    }

    fun createBoostTap(playerId: String, timestampMs: Long): String {
        val json = JSONObject()
        json.put("type", TYPE_BOOST_TAP)
        json.put("playerId", playerId)
        json.put("timestampMs", timestampMs)
        return json.toString()
    }

    fun createNextRound(hostPlayerId: String): String {
        val json = JSONObject()
        json.put("type", TYPE_NEXT_ROUND)
        json.put("playerId", hostPlayerId)
        return json.toString()
    }

    fun createLobbyUpdate(
        roomCode: String,
        roomName: String,
        players: List<Player>
    ): String {
        val json = JSONObject()
        json.put("type", TYPE_LOBBY_UPDATE)
        json.put("roomCode", roomCode)
        json.put("roomName", roomName)
        val arr = JSONArray()
        players.forEach { p ->
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("isHost", p.isHost)
            pObj.put("isReady", p.isReady)
            pObj.put("isBot", p.isBot)
            pObj.put("pingMs", p.pingMs)
            pObj.put("totalScore", p.totalScore)
            arr.put(pObj)
        }
        json.put("players", arr)
        return json.toString()
    }
}

