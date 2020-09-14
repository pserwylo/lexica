package net.healeys.trie;

import com.serwylo.lexica.lang.Language;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StringTrie extends Trie {

    final Node rootNode;

    public StringTrie(Language language) {
        super(language);
        rootNode = new Node(language);
    }

    /**
     * Decides whether you can transition from one cell to another, purely based on whether both the
     * source and destination cell are present on the board.
     * <p>
     * It doesn't have enough information to figure out which words can and can't be done correctly.
     * However it does have enough information to exclude large portions of a dictionary-sized
     * trie very quickly, instead of spending time reading and parsing it.
     * Using this approximately halves the loading time in my basic tests.
     */
    private static class CheapTransitionMap {

        private Map<String, Set<String>> transitions = new HashMap<>();

        CheapTransitionMap(TransitionMap transitionMap) {
            for (int fromPos = 0; fromPos < transitionMap.getSize(); fromPos++) {

                String from = transitionMap.valueAt(fromPos);

                int fromX = fromPos % transitionMap.getWidth();
                int fromY = fromPos / transitionMap.getWidth();

                Set<String> transitionTo = new HashSet<>();
                for (int j = 0; j < transitionMap.getSize(); j++) {
                    String to = transitionMap.valueAt(j);
                    if (transitionMap.valueAt(j).equals(to)) {
                        int toX = j % transitionMap.getWidth();
                        int toY = j / transitionMap.getWidth();
                        if (transitionMap.canTransition(fromX, fromY, toX, toY)) {
                            transitionTo.add(transitionMap.valueAt(j));
                        }
                    }
                }

                if (!transitions.containsKey(from)) {
                    transitions.put(from, new HashSet<>());
                }

                transitions.get(from).addAll(transitionTo);

            }
        }

        boolean contains(String from) {
            return transitions.containsKey(from);
        }

        boolean canTransition(String from, String to) {
            Set<String> transitionTo = transitions.get(from);
            return transitionTo != null && transitionTo.contains(to);
        }
    }

    private StringTrie(Language language, InputStream in, TransitionMap transitionMap) throws IOException {
        super(language);

        Set<String> availableStrings = new HashSet<>(transitionMap.getSize());
        for (int i = 0; i < transitionMap.getSize(); i++) {
            availableStrings.add(transitionMap.valueAt(i));
        }
        rootNode = new Node(new DataInputStream(new BufferedInputStream(in)), language, new CheapTransitionMap(transitionMap), availableStrings, false, null, 0);
    }

    @Override
    public void addWord(String w) {
        rootNode.addSuffix(w, 0);
    }

    @Override
    public boolean isWord(String word) {
        return rootNode.isAnyWord(word, 0);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        rootNode.writeNode(out);
    }

    public static class StringSolution implements net.healeys.trie.Solution {

        private final String word;
        private final Integer[] positions;

        public StringSolution(String word, Integer[] positions) {
            this.word = word;
            this.positions = positions;
        }

        @Override
        public String getWord() {
            return word;
        }

        public Integer[] getPositions() {
            return positions;
        }
    }

    private void recursiveSolver(TransitionMap transitions, WordFilter wordFilter, StringTrie.Node node, int pos, Set<Integer> usedPositions, StringBuilder prefix, Map<String, List<Solution>> solutions, List<Integer> solution) {

        if (node.word()) {
            String w = new String(prefix);
            if (wordFilter == null || wordFilter.isWord(w)) {
                Integer[] solutionArray = new Integer[solution.size()];
                solution.toArray(solutionArray);
                List<Solution> sols = solutions.get(w);
                if (sols == null) {
                    sols = new LinkedList<>();
                    solutions.put(w, sols);
                }
                sols.add(new StringSolution(w, solutionArray));
            }
        }

        if (node.isTail()) {
            return;
        }

        if (!transitions.canRevisit()) {
            usedPositions.add(pos);
        }

        int fromX = pos % transitions.getWidth();
        int fromY = pos / transitions.getWidth();

        for (int toX = 0; toX < transitions.getWidth(); toX++) {
            for (int toY = 0; toY < transitions.getWidth(); toY++) {
                if (!transitions.canTransition(fromX, fromY, toX, toY)) {
                    continue;
                }

                int toPosition = toX + transitions.getWidth() * toY;
                if (usedPositions.contains(toPosition)) {
                    continue;
                }

                String valueAt = transitions.valueAt(toPosition);
                StringTrie.Node nextNode = node.maybeChildAt(valueAt);
                if (nextNode == null) {
                    continue;
                }

                prefix.append(valueAt);

                solution.add(toPosition);
                recursiveSolver(transitions, wordFilter, nextNode, toPosition, usedPositions, prefix, solutions, solution);
                solution.remove(solution.size() - 1);

                prefix.delete(prefix.length() - valueAt.length(), prefix.length());
            }
        }

        usedPositions.remove(pos);
    }

    @Override
    public Map<String, List<Solution>> solver(TransitionMap transitions, WordFilter filter) {

        Map<String, List<Solution>> solutions = new TreeMap<>();
        StringBuilder prefix = new StringBuilder(transitions.getSize() + 1);

        List<Integer> positions = new ArrayList<>(transitions.getSize());
        for (int i = 0; i < transitions.getSize(); i++) {
            String value = transitions.valueAt(i);

            StringTrie.Node nextNode = rootNode.maybeChildAt(value);
            if (nextNode == null) {
                continue;
            }

            prefix.append(value);
            positions.add(i);

            recursiveSolver(transitions, filter, nextNode, i, new HashSet<>(), prefix, solutions, positions);

            positions.remove(positions.size() - 1);
            prefix.delete(prefix.length() - value.length(), prefix.length());
        }

        return solutions;
    }

    static class Node extends TrieNode {

        final Map<String, Node> children = new HashMap<>();

        boolean isWord;

        private Node(Language language) {
            super(language);
        }

        private Node(DataInputStream input, Language language, CheapTransitionMap transitionMap, Set<String> availableStrings, boolean shouldSkip, String lastChar, int depth) throws IOException {
            super(language);

            int nodeSizeInBytes = input.readInt();

            if (shouldSkip) {
                input.skipBytes(nodeSizeInBytes);
                return;
            }

            isWord = input.readBoolean();

            int numChildren = input.readShort();

            if (numChildren > 0) {
                String[] childStrings = new String[numChildren];
                for (int i = 0; i < numChildren; i++) {
                    int length = input.readByte();

                    byte[] bytes = new byte[length];
                    input.readFully(bytes);

                    String string = new String(bytes);
                    if (depth == 0 && transitionMap.contains(string) || depth > 0 && transitionMap.canTransition(lastChar, string)) {
                        childStrings[i] = string;
                    }
                }

                for (int i = 0; i < numChildren; i++) {
                    // Need to read the node regardless of whether we end up keeping it. This is to
                    // ensure that we traverse the InputStream in the right order.
                    boolean shouldSkipChild = childStrings[i] == null;
                    Node childNode = new Node(input, language, transitionMap, availableStrings, shouldSkipChild, childStrings[i], depth + 1);
                    if (!shouldSkipChild) {
                        children.put(childStrings[i], childNode);
                    }
                }
            }
        }

        @Override
        public void writeNode(OutputStream output) throws IOException {

            ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
            DataOutputStream tempOutputData = new DataOutputStream(tempOutput);

            tempOutputData.writeBoolean(isWord);

            tempOutputData.writeShort(children.size());
            Set<Map.Entry<String, Node>> entries = children.entrySet();
            for (Map.Entry<String, Node> entry : entries) {
                String character = entry.getKey();
                byte[] characterBytes = character.getBytes("UTF-8");
                tempOutputData.writeByte(characterBytes.length);
                for (byte b : characterBytes) {
                    tempOutputData.writeByte(b);
                }
            }

            for (Map.Entry<String, Node> entry : entries) {
                entry.getValue().writeNode(tempOutputData);
            }

            DataOutputStream outputData = new DataOutputStream(output);
            outputData.writeInt(tempOutput.size());
            outputData.write(tempOutput.toByteArray());
        }

        @Override
        public TrieNode addSuffix(String word, int currentPosition) {
            Node child = ensureChildAt(word, currentPosition);

            if (currentPosition == word.length() - 1) {
                child.isWord = true;
                return child;
            } else {
                return child.addSuffix(word, nextPosition(word, currentPosition));
            }
        }

        private int nextPosition(String word, int currentPosition) {
            return currentPosition + getCharAt(word, currentPosition).length();
        }

        private String getCharAt(String word, int position) {
            String character = Character.toString(word.charAt(position));
            String characterWithSuffix = language.applyMandatorySuffix(character);
            if (!character.equals(characterWithSuffix) && word.length() >= position + characterWithSuffix.length() && word.substring(position, position + characterWithSuffix.length()).equals(characterWithSuffix)) {
                return characterWithSuffix;
            }
            return character;
        }

        /**
         * Perhaps return multiple children, to deal with the normalization of unicode characters with diacritics.
         * <p>
         * For example, if we search for the letter "e", then we want to return Trie nodes for both "e" and "é" if they
         * exist.
         *
         * @param strict If true, only return a single node with exact matching diacritics (if any).
         */
        private List<Node> maybeChildAt(String word, int position, boolean strict) {
            String character = getCharAt(word, position);

            if (strict) {
                Node node = children.get(character);
                return node == null ? new ArrayList<>() : Collections.singletonList(node);
            }

            List<Node> nodes = new ArrayList<>(2);

            for (String c : children.keySet()) {
                if (c.equals(character)) {
                    nodes.add(children.get(c));
                } else if (!Normalizer.isNormalized(c, Normalizer.Form.NFKD)) {
                    String normalized = Normalizer.normalize(c, Normalizer.Form.NFKD);
                    char letter = normalized.charAt(0);
                    if (letter == character.charAt(0)) {
                        nodes.add(children.get(c));
                    }
                }
            }

            return nodes;
        }

        private Node maybeChildAt(String childChar) {
            return children.get(childChar);
        }

        private Node ensureChildAt(String word, int position) {
            String character = getCharAt(word, position);
            List<Node> existingNode = maybeChildAt(word, position, true);
            if (existingNode.size() == 0) {
                Node node = new Node(language);
                children.put(character, node);
                return node;
            } else {
                return existingNode.get(0);
            }
        }

        @Override
        public boolean word() {
            return isWord;
        }

        @Override
        public boolean isTail() {
            return children.size() == 0;
        }

        private boolean isAnyWord(String word, int currentPosition) {
            if (currentPosition == word.length()) {
                return isWord;
            }

            List<Node> childNodes = maybeChildAt(word, currentPosition, false);
            for (Node child : childNodes) {
                if (child.isAnyWord(word, nextPosition(word, currentPosition))) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String toString() {
            return this.isWord ? "Word with " + this.children.size() + " children" : "Node with " + this.children.size() + " children";
        }
    }

    public static class Deserializer implements net.healeys.trie.Deserializer<StringTrie> {
        @Override
        public StringTrie deserialize(InputStream stream, TransitionMap transitionMap, Language language) throws IOException {
            return new StringTrie(language, stream, transitionMap);
        }
    }

}
