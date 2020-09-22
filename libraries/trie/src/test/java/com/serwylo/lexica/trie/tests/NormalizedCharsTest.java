package com.serwylo.lexica.trie.tests;

import com.serwylo.lexica.lang.French;

import net.healeys.trie.StringTrie;
import net.healeys.trie.TransitionMap;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NormalizedCharsTest extends TrieTest {

    @Test
    public void strictMatchingRequiresDiacritics() {
        StringTrie trie = new StringTrie(new French());

        addWords(trie, new String[]{"déjà"});

        assertFalse(trie.isWord("deja", true));
        assertTrue(trie.isWord("déjà", true));
        assertFalse(trie.isWord("déjz", true));
    }

    @Test
    public void nonStrictMatchingIgnoresDiacritics() {
        StringTrie trie = new StringTrie(new French());

        addWords(trie, new String[]{"déjà"});

        assertTrue(trie.isWord("deja", false));
        assertTrue(trie.isWord("déjà", false));
        assertFalse(trie.isWord("déjz", false));
    }

}
