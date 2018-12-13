package edu.kit.minijava.transformation;

import firm.*;
import firm.nodes.*;

import java.util.*;

public class ConstantFolder extends ConstantFolderBase {
    ConstantFolder(Graph graph) {
        super(graph);

        BackEdges.enable(graph);
        this.iterate();
        BackEdges.disable(graph);

        this.debugLog();

        for (Node node : this.getTopologicalOrdering()) {
            TargetValue value = this.getValueForNode(node);

            // For efficiency reasons, do not replace nodes that were constants already.
            if (node instanceof Const) {
                assert ((Const)node).getTarval().equals(value);
                continue;
            }

            if (value.isConstant()) {
                Proj memoryBeforeOperation = this.getMemoryUsedByOperation(node).orElse(null);
                Const replacement = (Const)graph.newConst(value);

                safeReplaceNodeWithConstant(node, replacement, memoryBeforeOperation);
            }
        }
    }

    private TargetValue resultOfLastVisitedNode = null;

    private void iterate() {
        while (!this.isWorklistEmpty()) {
            this.resultOfLastVisitedNode = null;

            Node node = this.removeElementFromWorklist();
            node.accept(this);

            assert this.resultOfLastVisitedNode != null;

            // If a change was made, make sure to revisit affected edges.
            if (this.setValueForNode(node, this.resultOfLastVisitedNode)) {
                for (BackEdges.Edge edge : BackEdges.getOuts(node)) {
                    this.addElementToWorklist(edge.node);
                }
            }
        }
    }

    private Optional<Proj> getMemoryUsedByOperation(Node node) {
        Proj projection = null;

        if (node instanceof Div) {
            projection = (Proj)((Div)node).getMem();
        }
        else if (node instanceof Mod) {
            projection = (Proj)((Mod)node).getMem();
        }

        assert projection == null || projection.getNum() == 0;

        return Optional.ofNullable(projection);
    }

    @Override
    public void visit(Const node) {
        assert this.getValueForNode(node) == UNDEFINED;

        this.resultOfLastVisitedNode = node.getTarval();
    }

    @Override
    public void visit(Add node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::add);

        System.out.println(describe(left) + " + " + describe(right) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Sub node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::sub);

        System.out.println(describe(left) + " - " + describe(right) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Mul node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::mul);

        System.out.println(describe(left) + " * " + describe(right) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Div node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());
        TargetValue zero = new TargetValue(0, Mode.getIs());

        // Division by zero is undefined, we choose zero as result.
        if (!right.equals(zero)) {
            this.resultOfLastVisitedNode = fold(left, right, TargetValue::div);
        }
        else {
            this.resultOfLastVisitedNode = zero;
        }

        System.out.println(describe(left) + " / " + describe(right) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Mod node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());
        TargetValue zero = new TargetValue(0, Mode.getIs());

        // Division by zero is undefined, we choose zero as result.
        if (!right.equals(zero)) {
            this.resultOfLastVisitedNode = fold(left, right, TargetValue::mod);
        }
        else {
            this.resultOfLastVisitedNode = zero;
        }

        System.out.println(describe(left) + " % " + describe(right) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Minus node) {
        TargetValue other = this.getValueForNode(node.getOp());

        this.resultOfLastVisitedNode = fold(other, TargetValue::neg);

        System.out.println("-" + describe(other) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(And node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::and);

        System.out.println(describe(left) + " && " + describe(right) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Or node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::or);

        System.out.println(describe(left) + " || " + describe(right) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Not node) {
        TargetValue other = this.getValueForNode(node.getOp());

        this.resultOfLastVisitedNode = fold(other, TargetValue::not);

        System.out.println("!" + describe(other) + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Cmp node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, (lhs, rhs) -> {
            if (node.getRelation().contains(left.compare(right))) {
                return TargetValue.getBTrue();
            }
            else {
                return TargetValue.getBFalse();
            }
        });

        System.out.println(describe(left) + " " + node.getRelation() + " " + describe(right) + " = " +
                this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Proj projection) {
        if (projection.getMode().equals(Mode.getM())) {
            this.defaultVisit(projection);
        }
        else {
            this.resultOfLastVisitedNode = this.getValueForNode(projection.getPred());
        }
    }

    @Override
    public void visit(Conv node) {
        assert node.getPredCount() == 1;

        TargetValue oldValue = this.getValueForNode(node.getPred(0));
        TargetValue newValue = oldValue.convertTo(node.getMode());

        this.resultOfLastVisitedNode = newValue;
    }

    @Override
    public void defaultVisit(Node node) {
        this.resultOfLastVisitedNode = NOT_A_CONSTANT;
    }
}
