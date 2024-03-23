package com.serwylo.lexica;

import com.serwylo.lexica.game.FiveByFiveLetterGrid;
import com.serwylo.lexica.game.FourByFourLetterGrid;
import com.serwylo.lexica.game.LetterGrid;
import com.serwylo.lexica.lang.Persian;

import net.healeys.trie.Solution;
import net.healeys.trie.StringTrie;
import net.healeys.trie.WordFilter;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LetterGridTransitionTest {

    @Test
    public void persianBoard() {
        String[] board = new String[]{"و", "ﻗ", "ن", "ﻫ", "ﻒ", "ز", "ﺮ", "ا", "ﻒ", "غ", "ا", "م", "ﻒ", "ه", "ﺮ", "ی",};

        StringTrie trie = new StringTrie(new Persian());
        trie.addWord("وزغهایمان");

        TrieTest.assertTrieMatches("Contains Persian word وزغهایمان", trie, new String[]{"وزغهایمان"}, new String[]{"non Persian word"});

        Map<String, List<Solution>> solutions = trie.solver(new FourByFourLetterGrid(board), new WordFilter.MinLength(3));
        assertEquals(1, solutions.size());
    }

    @Test
    public void fiveByFive() {

        LetterGrid letterGrid = new FiveByFiveLetterGrid(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",});

        assertCanTransition(letterGrid, "A", "B", "F", "G");
        assertCannotTransition(letterGrid, "A", "C", "D", "E", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y");

        assertCanTransition(letterGrid, "B", "A", "F", "G", "H", "C");
        assertCannotTransition(letterGrid, "B", "D", "E", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y");

        assertCanTransition(letterGrid, "C", "B", "G", "H", "I", "D");
        assertCannotTransition(letterGrid, "C", "A", "E", "F", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y");
    }

    @Test
    public void fourByFour() {

        LetterGrid letterGrid = new FourByFourLetterGrid(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",});

        assertCanTransition(letterGrid, "A", "B", "E", "F");
        assertCannotTransition(letterGrid, "A", "C", "D", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P");

        assertCanTransition(letterGrid, "B", "A", "E", "F", "G", "C");
        assertCannotTransition(letterGrid, "B", "D", "H", "I", "J", "K", "L", "M", "N", "O", "P");

        assertCanTransition(letterGrid, "C", "B", "F", "G", "H", "D");
        assertCannotTransition(letterGrid, "C", "A", "E", "I", "J", "K", "L", "M", "N", "O", "P");

        assertCanTransition(letterGrid, "D", "C", "G", "H");
        assertCannotTransition(letterGrid, "D", "A", "B", "E", "F", "I", "J", "K", "L", "M", "N", "O", "P");

        assertCanTransition(letterGrid, "E", "A", "B", "F", "J", "I");
        assertCannotTransition(letterGrid, "E", "C", "D", "G", "H", "K", "L", "M", "N", "O", "P");

        assertCanTransition(letterGrid, "F", "A", "B", "C", "G", "K", "J", "I", "E");
        assertCannotTransition(letterGrid, "F", "D", "H", "L", "M", "N", "O");

        assertCanTransition(letterGrid, "G", "B", "C", "D", "H", "L", "K", "J", "F");
        assertCannotTransition(letterGrid, "G", "A", "E", "I", "M", "N", "O", "P");

        assertCanTransition(letterGrid, "H", "D", "C", "G", "K", "L");
        assertCannotTransition(letterGrid, "H", "A", "B", "E", "F", "I", "J", "M", "N", "O", "P");

        assertCanTransition(letterGrid, "I", "E", "F", "J", "N", "M");
        assertCannotTransition(letterGrid, "I", "A", "B", "C", "D", "G", "H", "K", "L", "O", "P");

        assertCanTransition(letterGrid, "J", "E", "F", "G", "K", "O", "N", "M", "I");
        assertCannotTransition(letterGrid, "J", "A", "B", "C", "D", "H", "L", "P");

        assertCanTransition(letterGrid, "K", "F", "G", "H", "L", "P", "O", "N", "J");
        assertCannotTransition(letterGrid, "K", "A", "B", "C", "D", "E", "I");

        assertCanTransition(letterGrid, "L", "H", "G", "K", "O", "P");
        assertCannotTransition(letterGrid, "L", "A", "B", "C", "D", "E", "F", "I", "J", "M", "N");

        assertCanTransition(letterGrid, "M", "I", "J", "N");
        assertCannotTransition(letterGrid, "M", "A", "B", "C", "D", "E", "F", "G", "H", "K", "L", "O", "P");

        assertCanTransition(letterGrid, "N", "M", "I", "J", "K", "O");
        assertCannotTransition(letterGrid, "N", "A", "B", "C", "D", "E", "F", "G", "H", "L", "P");

        assertCanTransition(letterGrid, "O", "N", "J", "K", "L", "P");
        assertCannotTransition(letterGrid, "O", "A", "B", "C", "D", "E", "F", "G", "H", "I", "M");

        assertCanTransition(letterGrid, "P", "O", "K", "L");
        assertCannotTransition(letterGrid, "P", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "M", "N");

    }

    private void assertCanTransition(LetterGrid letterGrid, String from, String... to) {
        for (String toLetter : to) {
            assertCanTransition(letterGrid, from, toLetter);
        }
    }

    private void assertCannotTransition(LetterGrid letterGrid, String from, String... to) {
        for (String toLetter : to) {
            assertCannotTransition(letterGrid, from, toLetter);
        }
    }

    private void assertCanTransition(LetterGrid letterGrid, String from, String to) {
        int fromPosition = positionOf(letterGrid, from);
        int toPosition = positionOf(letterGrid, to);

        int fromX = fromPosition % letterGrid.getWidth();
        int fromY = fromPosition / letterGrid.getWidth();

        int toX = toPosition % letterGrid.getWidth();
        int toY = toPosition / letterGrid.getWidth();

        assertCanTransition(letterGrid, fromX, fromY, toX, toY);
    }

    private void assertCannotTransition(LetterGrid letterGrid, String from, String to) {
        int fromPosition = positionOf(letterGrid, from);
        int toPosition = positionOf(letterGrid, to);

        int fromX = fromPosition % letterGrid.getWidth();
        int fromY = fromPosition / letterGrid.getWidth();

        int toX = toPosition % letterGrid.getWidth();
        int toY = toPosition / letterGrid.getWidth();

        assertCannotTransition(letterGrid, fromX, fromY, toX, toY);
    }

    private int positionOf(LetterGrid letterGrid, String letter) {
        for (int i = 0; i < letterGrid.getSize(); i++) {
            if (letterGrid.elementAt(i).equals(letter)) {
                return i;
            }
        }

        throw new IllegalArgumentException("Letter \"" + letter + "\" not on board.");
    }

    private void assertCanTransition(LetterGrid letterGrid, int fromX, int fromY, int toX, int toY) {
        String from = letterGrid.elementAt(xyToPosition(letterGrid, fromX, fromY));
        String to = letterGrid.elementAt(xyToPosition(letterGrid, toX, toY));
        assertTrue(from + " should be able to transition to " + to, letterGrid.canTransition(fromX, fromY, toX, toY));
    }

    private void assertCannotTransition(LetterGrid letterGrid, int fromX, int fromY, int toX, int toY) {
        String from = letterGrid.elementAt(xyToPosition(letterGrid, fromX, fromY));
        String to = letterGrid.elementAt(xyToPosition(letterGrid, toX, toY));
        assertFalse(from + " should not be able to transition to " + to, letterGrid.canTransition(fromX, fromY, toX, toY));
    }

    private static int xyToPosition(LetterGrid letterGrid, int x, int y) {
        return x + letterGrid.getWidth() * y;
    }

}
