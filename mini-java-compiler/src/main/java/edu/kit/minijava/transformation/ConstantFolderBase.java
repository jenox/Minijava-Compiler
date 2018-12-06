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
        TargetValue oldValue = this.getValueForNode(node);
        TargetValue newValue = join(oldValue, value);

        System.out.println(describe(oldValue) + " ⊔ " + describe(value) + " = " + describe(newValue));

        this.values.put(node, value);

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

    Set<Node> getNodes() {
        return this.values.keySet();
    }

    void debugLog() {
        System.out.println(this.worklist);
        System.out.println(this.values);
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

    // TODO: what happens if we divide by zero?
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
            return "undefined";
        }
        else if (value == NOT_A_CONSTANT) {
            return "not a constant";
        }
        else {
            return value.toString();
        }
    }

}
