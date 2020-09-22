package net.healeys.trie;

import com.serwylo.lexica.lang.Language;

import java.io.IOException;
import java.io.InputStream;

public interface Deserializer<T extends Trie> {

    /**
     * Given an input stream that comprises of a full dictionary converted into a trie of some sort,
     * this deserializes a subset of the full trie. The subset contains only the letters in
     * {@param lettersToKeep}.
     *
     * @param strict If true, then letters with diacritics must match exactly. Otherwise, use language specific
     *               normalisation rules (typically those rules used by crosswords).
     */
    T deserialize(InputStream stream, TransitionMap transitionMap, Language language, boolean strict) throws IOException;

}
