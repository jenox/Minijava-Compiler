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
        this.visit(node, "+", TargetValue::add);
    }

    @Override
    public void visit(Sub node) {
        this.visit(node, "-", TargetValue::sub);
    }

    private void visit(Binop node, String operator, ConstantFolderBase.BinaryOperation operation) {
        TargetValue left = this.getValueForNode(node.getLeft());
        TargetValue right = this.getValueForNode(node.getRight());
        TargetValue result = fold(left, right, operation);

        this.resultOfLastVisitedNode = result;

        System.out.println(left + " " + operator + " " + right + " = " + result);
    }
}
