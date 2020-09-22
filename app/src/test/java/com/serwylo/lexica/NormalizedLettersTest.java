package com.serwylo.lexica;

import com.serwylo.lexica.game.Board;
import com.serwylo.lexica.game.FourByFourBoard;
import com.serwylo.lexica.lang.DeGerman;
import com.serwylo.lexica.lang.EnglishUS;
import com.serwylo.lexica.lang.French;
import com.serwylo.lexica.lang.Language;

import net.healeys.trie.Solution;
import net.healeys.trie.StringTrie;
import net.healeys.trie.Trie;
import net.healeys.trie.WordFilter;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NormalizedLettersTest {

    private static final Board NORMALISED_BOARD = new FourByFourBoard(new String[]{"d", "e", "j", "a", "z", "z", "z", "z", "z", "z", "z", "z", "z", "z", "z", "z",});
    private static final Board STRICT_BOARD = new FourByFourBoard(new String[]{"d", "é", "j", "à", "z", "z", "z", "z", "z", "z", "z", "z", "z", "z", "z", "z",});

    private static final boolean strict = true;
    private static final boolean notStrict = false;

    @Test
    public void strictMatchesRequiresDiacritics() {
        Trie trie = new StringTrie(new French());
        TrieTest.addWords(trie, new String[] { "déjà" });

        assertFalse(trie.isWord("deja", strict));
        assertTrue(trie.isWord("déjà", strict));

        Map<String, List<Solution>> solutions = trie.solver(STRICT_BOARD, new WordFilter.MinLength(3), true);
        assertEquals(Collections.singleton("déjà"), solutions.keySet());
    }

    @Test
    public void nonStrictMatchesIgnoreDiacritics() {
        Trie trie = new StringTrie(new French());
        TrieTest.addWords(trie, new String[] { "déjà" });

        assertTrue(trie.isWord("deja", notStrict));
        assertTrue(trie.isWord("déjà", notStrict));

        Map<String, List<Solution>> solutions = trie.solver(NORMALISED_BOARD, new WordFilter.MinLength(3), false);
        assertEquals(Collections.singleton("déjà"), solutions.keySet());
    }

}
