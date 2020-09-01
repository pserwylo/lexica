package com.serwylo.lexica.trie.tests;

import com.serwylo.lexica.lang.French;

import net.healeys.trie.StringTrie;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NormalizedCharsTest extends TrieTest {

    @Test
    public void testNormalization() {
        StringTrie trie = new StringTrie(new French());

        addWords(trie, new String[]{"déjà"});

        assertTrue(trie.isWord("deja"));
        assertTrue(trie.isWord("déjà"));
        assertFalse(trie.isWord("déjz"));
    }

}
