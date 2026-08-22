package com.example.network

import com.example.model.GamePlayState
import com.example.model.Player
import java.util.Collections

class GameEngine {

    companion object {
        val WORD_SUGGESTIONS = listOf(
            "TIGER", "MOON", "APPLE", "CHESS", "SKY", "CROWN",
            "DRAGON", "STORM", "NEON", "PIXEL", "CYBER", "COMET",
            "FALCON", "BLADE", "VIPER", "TITAN", "SOLAR", "SHADOW"
        )

        fun validateWord(word: String, existingWords: List<String>): Pair<Boolean, String> {
            val trimmed = word.trim().uppercase()
            if (trimmed.length < 3) {
                return Pair(false, "Minimum 3 letters required")
            }
            if (trimmed.length > 12) {
                return Pair(false, "Maximum 12 letters allowed")
            }
            if (!trimmed.all { it in 'A'..'Z' }) {
                return Pair(false, "Letters only (no numbers or emojis)")
            }
            if (existingWords.map { it.trim().uppercase() }.contains(trimmed)) {
                return Pair(false, "Word already chosen by another player")
            }
            return Pair(true, "")
        }

        /**
         * Creates and shuffles deck such that:
         * 1. Each word appears exactly 3 times.
         * 2. Total cards = 3 * players.size
         * 3. NO player receives 3 identical cards initially.
         */
        fun dealInitialCards(players: List<Player>): Map<String, List<String>> {
            val words = players.map { it.submittedWord.trim().uppercase() }
            val totalPlayers = players.size
            val cardHands = mutableMapOf<String, List<String>>()

            var attempts = 0
            var validDeal = false

            while (!validDeal && attempts < 200) {
                attempts++
                val deck = mutableListOf<String>()
                for (word in words) {
                    repeat(3) { deck.add(word) }
                }
                deck.shuffle()

                val tempHands = mutableMapOf<String, List<String>>()
                var hasTriplicate = false

                for (i in 0 until totalPlayers) {
                    val hand = deck.subList(i * 3, (i + 1) * 3).toList()
                    // Check if hand has all 3 identical words
                    if (hand.size == 3 && hand[0] == hand[1] && hand[1] == hand[2]) {
                        hasTriplicate = true
                        break
                    }
                    tempHands[players[i].id] = hand
                }

                if (!hasTriplicate && tempHands.size == totalPlayers) {
                    cardHands.putAll(tempHands)
                    validDeal = true
                }
            }

            // Fallback in rare case: guarantee no triplicates by simple rotation
            if (!validDeal) {
                cardHands.clear()
                for (i in 0 until totalPlayers) {
                    val w1 = words[i % totalPlayers]
                    val w2 = words[(i + 1) % totalPlayers]
                    val w3 = words[(i + 2) % totalPlayers]
                    cardHands[players[i].id] = listOf(w1, w2, w3)
                }
            }

            return cardHands
        }

        /**
         * Checks if a player has 3 identical cards
         */
        fun checkWinningHand(cards: List<String>): String? {
            if (cards.size == 3 && cards[0] == cards[1] && cards[1] == cards[2]) {
                return cards[0]
            }
            return null
        }
    }
}
