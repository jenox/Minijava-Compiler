package edu.kit.minijava.transformation;

import firm.nodes.*;

import java.util.*;

public class NodeCollector extends NodeVisitor.Default {
    private final List<Node> nodes = new ArrayList<>();

    public List<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public void defaultVisit(Node node) {
        this.nodes.add(node);
    }
}
