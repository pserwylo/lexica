package com.serwylo.lexica.game

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.SparseIntArray
import com.serwylo.lexica.db.GameMode
import com.serwylo.lexica.lang.Language
import net.healeys.trie.Solution
import net.healeys.trie.StringTrie
import net.healeys.trie.Trie
import java.io.IOException

private const val TAG = "Board"

data class Board(
    val letterGrid: LetterGrid,
    val solutions: Map<String, List<Solution>>,
    val maxWordCountByLength: SparseIntArray,
)

/**
 * This function tries to be as lean as possible, so that we can generate multiple candidate boards
 * to evaluate before choosing one for a new game. The faster it executes, the more candidates we
 * can evaluate.
 *
 * Having said that, there is still disk access in it due to the way in which it loads a trie from
 * disk. When loading, it loads a filtered trie which only contains the words found on a given board.
 * Perhaps this could be improved in the future by loading the full trie for a language once upfront
 * (like we do with the [charProbs]) and then filtering it each time we create a board.
 */
fun createBoard(context: Context, language: Language, charProbs: CharProbGenerator, gameMode: GameMode): Board {
    val letterGrid = createLetterGrid(gameMode.boardSize, charProbs)
    return createBoard(context, language, letterGrid, gameMode)
}

fun createBoard(context: Context, language: Language, letters: Array<String>, gameMode: GameMode): Board {
    val letterGrid = createLetterGrid(letters)
    return createBoard(context, language, letterGrid, gameMode)
}

@SuppressLint("DiscouragedApi") // Context.resources.getIdentifier required due to different language selections.
private fun createBoard(context: Context, language: Language, letterGrid: LetterGrid, gameMode: GameMode): Board {
    val dict: Trie = try {
        val trieFileName = language.trieFileName
        val dictResId = "raw/" + trieFileName.substring(0, trieFileName.lastIndexOf('.'))
        val id = context.resources.getIdentifier(dictResId, null, context.packageName)
        StringTrie.Deserializer().deserialize(context.resources.openRawResource(id), letterGrid, language)
    } catch (e: IOException) {
        Log.e(TAG, "Error initializing dictionary", e)
        throw java.lang.IllegalStateException("Error initializing dictionary", e)
    }

    val solutions = dict.solver(letterGrid) { w: String -> w.length >= gameMode.minWordLength }
    Log.d(TAG, "Initializing " + language.name + " dictionary")

    val maxWordCountsByLength = SparseIntArray()
    for (word in solutions.keys) {
        maxWordCountsByLength.put(word.length, maxWordCountsByLength[word.length] + 1)
    }

    return Board(letterGrid, solutions, maxWordCountsByLength)
}

@SuppressLint("DiscouragedApi") // Context.resources.getIdentifier required due to different language selections.
fun loadCharProps(context: Context, language: Language): CharProbGenerator {
    val lettersFileName = language.letterDistributionFileName
    val id: Int = context.resources.getIdentifier(
        "raw/" + lettersFileName.substring(0, lettersFileName.lastIndexOf('.')),
        null,
        context.packageName
    )
    return CharProbGenerator(context.resources.openRawResource(id), language)
}

fun createLetterGrid(boardSize: Int, charProbs: CharProbGenerator): LetterGrid =
    when (boardSize) {
        16 -> charProbs.generateFourByFourBoard()
        25 -> charProbs.generateFiveByFiveBoard()
        36 -> charProbs.generateSixBySixBoard()
        else -> throw IllegalStateException("Board must be 16, 25, or 36 large")
    }

fun createLetterGrid(letters: Array<String>): LetterGrid =
    when (letters.size) {
        16 -> FourByFourLetterGrid(letters)
        25 -> FiveByFiveLetterGrid(letters)
        36 -> SixBySixLetterGrid(letters)
        else -> throw java.lang.IllegalStateException("Board must be 16, 25, or 36 large")
    }