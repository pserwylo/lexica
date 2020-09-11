package com.serwylo.lexica.trie.util;

import com.serwylo.lexica.lang.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * For each word, we count up the number of times each character appears in the word.
 * The more often this happens, the more likely it should be that we show it to users
 * on the game board.
 */
public class LetterFrequency {

    private final Language language;
    private final HashMap<String, List<Integer>> letterCounts = new HashMap<>();
    private HashMap<String, Integer> totalLetterCounts = null;
    private int maxCount = 0;

    public LetterFrequency(Language language) {
        this.language = language;
    }

    public void addWord(String word) {
        HashMap<String, Integer> counts = getLetterCountsForWord(word);
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String letter = entry.getKey();
            int count = entry.getValue();

            if (!letterCounts.containsKey(letter)) {
                letterCounts.put(letter, new ArrayList<>());
            }

            // Ensure there are enough values populated in the list.
            List<Integer> list = letterCounts.get(letter);
            if (list.size() < count) {
                for (int i = list.size(); i < count; i++) {
                    list.add(i, 0);
                }
            }

            int index = count - 1;
            int previousValue = list.remove(index);
            list.add(index, previousValue + 1);
        }
    }

    public Set<String> getLetters() {
        return letterCounts.keySet();
    }

    public List<Integer> getCountsForLetter(String letter) {
        if (!letterCounts.containsKey(letter)) {
            return Collections.emptyList();
        }

        return letterCounts.get(letter);
    }

    private void ensureTotalLetterCounts() {
        if (totalLetterCounts == null) {
            totalLetterCounts = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : letterCounts.entrySet()) {
                List<Integer> countsForLetter = getCountsForLetter(entry.getKey());
                int totalCountForLetter = 0;
                for (int i = 1; i < countsForLetter.size(); i ++) {
                    totalCountForLetter += countsForLetter.get(i);
                }

                totalLetterCounts.put(entry.getKey(), totalCountForLetter);
            }

            for (Map.Entry<String, Integer> entry : totalLetterCounts.entrySet()) {
                int count = entry.getValue();
                if (count > maxCount) {
                    maxCount = count;
                }
            }

        }
    }

    public int getTotalCountForLetter(String letter) {
        ensureTotalLetterCounts();
        return totalLetterCounts.get(letter);
    }

    public int getMaxCount() {
        ensureTotalLetterCounts();
        return maxCount;
    }

    public HashMap<String, Integer> getLetterCountsForWord(String word) {
        HashMap<String, Integer> counts = new HashMap<>();

        for (int i = 0; i < word.length(); i++) {
            String letter = word.substring(i, i + 1);
            String letterWithSuffix = language.applyMandatorySuffix(letter);

            if (shouldInclude(word, i)) {
                if (!counts.containsKey(letterWithSuffix)) {
                    counts.put(letter, 1);
                } else {
                    counts.put(letter, counts.get(letterWithSuffix) + 1);
                }
            }

            if (!letter.equals(letterWithSuffix)) {
                i += letterWithSuffix.length() - 1;
            }
        }

        return counts;
    }

    public boolean shouldInclude(String word, int position) {
        String letter = word.substring(position, position + 1);
        String letterWithSuffix = language.applyMandatorySuffix(letter);

        // If a word has a "qu", then add the "q" to our letter frequencies, but not
        // the "u" (because it is useless without a "q" for the purposes of this word.
        if (!letterWithSuffix.equals(letter)) {

            // Some words may have a "q" without a "u". We will not add the "q"
            // to our letter frequencies if this is the case.
            boolean shouldInclude = true;
            for (int i = 0; i < letterWithSuffix.length(); i++) {
                if (word.length() <= position + i || word.charAt(position + i) != letterWithSuffix.charAt(i)) {
                    shouldInclude = false;
                    break;
                }
            }

            return shouldInclude;
        }

        return true;
    }

}
