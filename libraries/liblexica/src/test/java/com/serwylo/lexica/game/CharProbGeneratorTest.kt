package com.serwylo.lexica.game

import com.serwylo.lexica.game.CharProbGenerator.ProbabilityQueue
import com.serwylo.lexica.lang.EnglishGB
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CharProbGeneratorTest {

    @Test
    fun testCharProbGenerator() {

        val probString = """
            a 100 20 1 1
            b 4
            c 2 1
            d 5 4 3 2 1
            e 1 1 1 1
        """.trimIndent()

        val generator = CharProbGenerator(probString.byteInputStream(), EnglishGB())

        val expected = mapOf(
            "a" to listOf(100, 20, 1, 1),
            "b" to listOf(4),
            "c" to listOf(2, 1),
            "d" to listOf(5, 4, 3, 2, 1),
            "e" to listOf(1, 1, 1, 1),
        )

        assertEquals(expected, generator.distribution)

        // It is hard to test a random proecss like "generating a board from a probability distribution",
        // but we do know that if there are only 16 possible values in our distribution, and we ask for
        // a 4x4 board, then all of them should be present according to the available frequencies in
        // the distribution.
        val expectedLetters = listOf(
            "a", "a", "a", "a",
            "b", "c", "c",
            "d", "d", "d", "d", "d",
            "e", "e", "e", "e",
        )

        // We should be able to generate multiple boards from the same distribution. In the
        // past (pre-2024) generating a board was a destructive process for the distribution,
        // meaning it couldn't be used more than once and needed to be copied prior to being
        // used again. Now it should reset its internal state after generating a board each time.
        assertEquals(expectedLetters, generator.generateFourByFourBoard().letters.sorted())
        assertEquals(expectedLetters, generator.generateFourByFourBoard().letters.sorted())

    }

    @Test
    fun probabilityQueueTest() {
        val q = ProbabilityQueue("a")
        q.addProb("100")
        q.addProb("20")
        q.addProb("1")
        q.addProb("1")

        assertEquals("a", q.letter)

        assertEquals(100, q.peekProb())
        assertEquals(100, q.prob)

        assertEquals(20, q.peekProb())
        assertEquals(20, q.prob)

        assertEquals(1, q.peekProb())
        assertEquals(1, q.prob)

        assertEquals(1, q.peekProb())
        assertEquals(1, q.prob)

        assertEquals(0, q.peekProb())
        assertEquals(0, q.prob)

        assertEquals(0, q.peekProb())
        assertEquals(0, q.prob)

        q.reset()
        assertEquals(100, q.peekProb())
    }

}