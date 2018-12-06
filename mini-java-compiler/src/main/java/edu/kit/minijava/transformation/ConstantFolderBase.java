package edu.kit.minijava.transformation;

import firm.*;
import firm.nodes.*;

import java.util.*;

abstract class ConstantFolderBase extends NodeVisitor.Default {

    ConstantFolderBase(Graph graph) {
        NodeCollector collector = new NodeCollector();
        graph.walkTopological(collector);

        this.worklist = new ArrayDeque<>(collector.getNodes());
    }

    private final Queue<Node> worklist;
    private final Map<Node, ConstantValue> values = new HashMap<>();

    ConstantValue getValueForNode(Node node) {
        return Optional.ofNullable(this.values.get(node)).orElseGet(ConstantValue::undefined);
    }

    void setValueForNode(Node node, ConstantValue value) {
        this.values.put(node, value);
    }

    boolean isWorklistEmpty() {
        return this.worklist.isEmpty();
    }

    Node removeElementFromWorklist() {
        return this.worklist.remove();
    }

    void addElementToWorklist(Node node) {
        if (!this.worklist.contains(node)) {
            this.worklist.add(node);
        }
    }

    Set<Node> getNodes() {
        return this.values.keySet();
    }

    void debugLog() {
        System.out.println(this.worklist);
        System.out.println(this.values);
    }
}
