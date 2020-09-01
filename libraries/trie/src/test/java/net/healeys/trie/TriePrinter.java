package net.healeys.trie;

import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.TraditionalTreePrinter;

@SuppressWarnings("unused") // Debugging tool useful during authoring of test cases.
public class TriePrinter {

    private final SimpleTreeNode root;

    public TriePrinter(StringTrie trie) {
        root = new SimpleTreeNode("root");
        addNode(root, trie.rootNode);
    }

    private void addNode(SimpleTreeNode parent, StringTrie.Node toAdd) {
        toAdd.children.forEach((c, node) -> {
            SimpleTreeNode child = new SimpleTreeNode(c);
            parent.addChild(child);
            addNode(child, node);
        });
    }

    public void dump() {
        new TraditionalTreePrinter().print(root);
    }
}
