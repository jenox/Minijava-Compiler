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
    }

    private ConstantValue resultOfLastVisitedNode = null;

    private void iterate() {
        while (!this.isWorklistEmpty()) {
            this.resultOfLastVisitedNode = null;

            Node node = this.removeElementFromWorklist();
            node.accept(this);

            // Not all nodes are evaluated.
            if (this.resultOfLastVisitedNode != null) {
                ConstantValue result = this.resultOfLastVisitedNode;
                ConstantValue oldValue = this.getValueForNode(node);
                ConstantValue newValue = oldValue.join(result);

                System.out.println(oldValue + " ⊔ " + result + " = " + newValue);

                this.setValueForNode(node, newValue);

                // If a change was made, make sure to revisit affected edges.
                if (!oldValue.equals(newValue)) {
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
        assert this.getValueForNode(node).isUndefined();

        this.setValueForNode(node, ConstantValue.constant(node.getTarval()));
    }

    @Override
    public void visit(Add node) {
        this.visit(node, "+", TargetValue::add);
    }

    @Override
    public void visit(Sub node) {
        this.visit(node, "-", TargetValue::sub);
    }

    private void visit(Binop node, String operator, ConstantValue.BinaryOperation operation) {
        ConstantValue left = this.getValueForNode(node.getLeft());
        ConstantValue right = this.getValueForNode(node.getRight());
        ConstantValue result = ConstantValue.fold(left, right, operation);

        this.resultOfLastVisitedNode = result;

        System.out.println(left + " " + operator + " " + right + " = " + result);
    }
}
