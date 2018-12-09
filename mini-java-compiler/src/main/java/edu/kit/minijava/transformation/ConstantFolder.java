package edu.kit.minijava.transformation;

import firm.*;
import firm.nodes.*;

public class ConstantFolder extends ConstantFolderBase {
    ConstantFolder(Graph graph) {
        super(graph);

        BackEdges.enable(graph);
        this.iterate();
        BackEdges.disable(graph);

        this.debugLog();

        for (Node node : this.getNodes()) {
            TargetValue value = this.getValueForNode(node);

            if (value.isConstant()) {
                Graph.exchange(node, graph.newConst(value));
            }
        }
    }

    private TargetValue resultOfLastVisitedNode = null;

    private void iterate() {
        while (!this.isWorklistEmpty()) {
            this.resultOfLastVisitedNode = null;

            Node node = this.removeElementFromWorklist();
            node.accept(this);

            // Not all nodes are evaluated.
            if (this.resultOfLastVisitedNode != null) {

                // If a change was made, make sure to revisit affected edges.
                if (this.setValueForNode(node, this.resultOfLastVisitedNode)) {
                    System.out.println("value of " + node + " changed");

                    for (BackEdges.Edge edge : BackEdges.getOuts(node)) {
                        this.addElementToWorklist(edge.node);
                    }
                }
            }
        }
    }

    @Override
    public void visit(Const node) {
        assert this.getValueForNode(node) == UNDEFINED;

        this.setValueForNode(node, node.getTarval());
    }

    @Override
    public void visit(Add node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::add);

        System.out.println(left + " + " + right + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Sub node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::sub);

        System.out.println(left + " - " + right + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Mul node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::mul);

        System.out.println(left + " * " + right + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Div node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        // TODO: what happens if we divide by zero?
        this.resultOfLastVisitedNode = fold(left, right, TargetValue::div);

        System.out.println(left + " / " + right + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Mod node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::mod);

        System.out.println(left + " % " + right + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Minus node) {
        TargetValue other = this.getValueForNode(node.getOp());

        this.resultOfLastVisitedNode = fold(other, TargetValue::neg);

        System.out.println("-" + other + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(And node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::and);

        System.out.println(left + " && " + right + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Or node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        this.resultOfLastVisitedNode = fold(left, right, TargetValue::or);

        System.out.println(left + " || " + right + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Not node) {
        TargetValue other = this.getValueForNode(node.getOp());

        this.resultOfLastVisitedNode = fold(other, TargetValue::not);

        System.out.println("!" + other + " = " + this.resultOfLastVisitedNode);
    }

    @Override
    public void visit(Cmp node) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());

        if (node.getRelation().contains(left.compare(right))) {
            this.resultOfLastVisitedNode = new TargetValue(-1, Mode.getBs());
        }
        else {
            this.resultOfLastVisitedNode = new TargetValue(0, Mode.getBs());
        }

        System.out.println(left + " " + node.getRelation() + " " + right + " = " + this.resultOfLastVisitedNode);
    }
}
