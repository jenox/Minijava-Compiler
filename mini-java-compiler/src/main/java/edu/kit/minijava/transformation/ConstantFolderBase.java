package edu.kit.minijava.transformation;

import firm.*;
import firm.nodes.*;
import org.jetbrains.annotations.*;

import java.util.*;

abstract class ConstantFolderBase extends NodeVisitor.Default {

    ConstantFolderBase(Graph graph) {
        NodeCollector collector = new NodeCollector();
        graph.walkTopological(collector);

        this.topologicalOrdering = Collections.unmodifiableList(collector.getNodes());
        this.worklist = new ArrayDeque<>(collector.getNodes());
    }

    private final List<Node> topologicalOrdering;
    private final Queue<Node> worklist;
    private final Map<Node, TargetValue> values = new HashMap<>();

    TargetValue getValueForNode(Node node) {
        return Optional.ofNullable(this.values.get(node)).orElse(UNDEFINED);
    }

    /**
     * When attempting to set conflicting values, transitions to not a constant.
     *
     * Returns whether or not a change was made.
     */
    boolean setValueForNode(Node node, TargetValue value) {

        // Assert integrity of target value. Other parts of code assume shared identities for bottom (UNDEFINED) and top
        // (NOT_A_CONSTANT) values. Maybe we should change this? But at least we don't fail silently now.
        assert !value.equals(TargetValue.getUnknown()) || value.equals(UNDEFINED);
        assert !value.equals(TargetValue.getBad()) || value.equals(NOT_A_CONSTANT);

        TargetValue oldValue = this.getValueForNode(node);
        TargetValue newValue = join(oldValue, value);

//        System.out.println(describe(oldValue) + " ⊔ " + describe(value) + " = " + describe(newValue) + "\t" + node);

        this.values.put(node, newValue);

        return !oldValue.equals(newValue);
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

    List<Node> getTopologicalOrdering() {
        return this.topologicalOrdering;
    }

    void debugLog() {
        for (Node node : this.values.keySet()) {
            TargetValue value = this.values.get(node);

            if (value.isConstant() && !(node instanceof Const)) {
                //System.out.println(node + ": " + value);
            }
        }
    }


    // MARK: - Lattice

    static final TargetValue UNDEFINED = TargetValue.getUnknown();
    static final TargetValue NOT_A_CONSTANT = TargetValue.getBad();

    interface UnaryOperation {
        TargetValue perform(TargetValue value);
    }

    interface BinaryOperation {
        TargetValue perform(TargetValue lhs, TargetValue rhs);
    }

    static TargetValue fold(TargetValue left, TargetValue right, BinaryOperation operation) {
        if (left.isConstant() && right.isConstant()) {
            return operation.perform(left, right);
        }
        else if (left == NOT_A_CONSTANT || right == NOT_A_CONSTANT) {
            return NOT_A_CONSTANT;
        }
        else {
            return UNDEFINED;
        }
    }

    static TargetValue fold(TargetValue value, UnaryOperation operation) {
        if (value.isConstant()) {
            return operation.perform(value);
        }
        else if (value == NOT_A_CONSTANT) {
            return NOT_A_CONSTANT;
        }
        else {
            return UNDEFINED;
        }
    }

    // TODO: Unit tests!
    static TargetValue join(TargetValue left, TargetValue right) {
        if (left == NOT_A_CONSTANT || right == NOT_A_CONSTANT) {
            return NOT_A_CONSTANT;
        }
        else if (left.isConstant() && right.isConstant()) {
            if (left.equals(right)) {
                return left;
            }
            else {
                return NOT_A_CONSTANT;
            }
        }
        else if (left.isConstant()) {
            return left;
        }
        else if (right.isConstant()) {
            return right;
        }
        else {
            return UNDEFINED;
        }
    }

    static String describe(TargetValue value) {
        if (value == UNDEFINED) {
            return "⊥";
        }
        else if (value == NOT_A_CONSTANT) {
            return "⊤";
        }
        else {
            return value.toString();
        }
    }


    // MARK: - Graph Operations

    static void safeReplaceNodeWithConstant(Node oldValue, Const newValue, @Nullable Node memoryBeforeOperation) {
        //System.out.println("Replacing " + oldValue + " -> " + newValue);
        List<Node> successors = getSuccessorsOf(oldValue);

        Graph.exchange(oldValue, newValue);

        for (Node successor : successors) {
            if (!(successor instanceof Proj)) continue;

            Proj projection = (Proj)successor;

            // Contract attached memory projections.
            if (projection.getMode().equals(Mode.getM())) {
                assert memoryBeforeOperation != null;

                contractProjection(projection, memoryBeforeOperation);
            }
        }
    }

    private static void contractProjection(Proj projection, Node replacement) {
        assert projection != null;
        assert replacement != null;

        for (Node successor : getSuccessorsOf(projection)) {
            for (int index = 0; index < successor.getPredCount(); index += 1) {
                if (successor.getPred(index).equals(projection)) {
                    successor.setPred(index, replacement);
                }
            }
        }

        Graph.killNode(projection);
    }

    protected static List<Node> getSuccessorsOf(Node node) {
        Graph graph = node.getGraph();
        boolean wasEnabled = BackEdges.enabled(graph);

        if (!wasEnabled) {
            BackEdges.enable(graph);
        }

        List<Node> nodes = new ArrayList<>();

        for (BackEdges.Edge edge : BackEdges.getOuts(node)) {
            nodes.add(edge.node);
        }

        if (!wasEnabled) {
            BackEdges.disable(graph);
        }

        return nodes;
    }
}
