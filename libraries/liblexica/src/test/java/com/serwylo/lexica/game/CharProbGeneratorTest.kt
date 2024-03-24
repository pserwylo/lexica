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
        """.trimIndent()

        val generator = CharProbGenerator(probString.byteInputStream(), EnglishGB())

        val expected = mapOf(
            "a" to listOf(100, 20, 1, 1),
            "b" to listOf(4),
            "c" to listOf(2, 1),
        )

        assertEquals(expected, generator.distribution)

        val copy = CharProbGenerator(generator)
        assertEquals(expected, copy.distribution)

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
    }

}