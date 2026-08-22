package com.example

import com.example.model.Player
import com.example.network.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun testWordValidation() {
        val existingWords = listOf("TIGER", "MOON")

        // Valid word
        val (valid1, err1) = GameEngine.validateWord("APPLE", existingWords)
        assertTrue(valid1)
        assertTrue(err1.isEmpty())

        // Too short
        val (valid2, _) = GameEngine.validateWord("NO", existingWords)
        assertFalse(valid2)

        // Too long (>12 chars)
        val (valid3, _) = GameEngine.validateWord("SUPERCALIFRAGI", existingWords)
        assertFalse(valid3)

        // Contains numbers or symbols
        val (valid4, _) = GameEngine.validateWord("TIGER1", existingWords)
        assertFalse(valid4)

        // Duplicate word
        val (valid5, err5) = GameEngine.validateWord("TIGER", existingWords)
        assertFalse(valid5)
        assertTrue(err5.isNotEmpty())
    }

    @Test
    fun testCardDealingNoPlayerStartsWithThreeIdentical() {
        val players = listOf(
            Player(id = "1", name = "Praveen", submittedWord = "TIGER"),
            Player(id = "2", name = "Harish", submittedWord = "MOON"),
            Player(id = "3", name = "Karthik", submittedWord = "APPLE"),
            Player(id = "4", name = "Suresh", submittedWord = "SKY")
        )

        // Run 50 simulation deals
        for (i in 0 until 50) {
            val dealtMap = GameEngine.dealInitialCards(players)
            assertEquals(4, dealtMap.size)
            dealtMap.values.forEach { hand ->
                assertEquals(3, hand.size)
                // No player starts with 3 of the same card
                val isThreeOfAKind = hand[0] == hand[1] && hand[1] == hand[2]
                assertFalse("Player must not start with 3 identical cards", isThreeOfAKind)
            }
        }
    }

    @Test
    fun testCheckWinner() {
        val cards1 = listOf("TIGER", "MOON", "TIGER")
        val winner1 = GameEngine.checkWinningHand(cards1)
        assertNull(winner1)

        val cards2 = listOf("APPLE", "APPLE", "APPLE")
        val winner2 = GameEngine.checkWinningHand(cards2)
        assertNotNull(winner2)
        assertEquals("APPLE", winner2)
    }
}

