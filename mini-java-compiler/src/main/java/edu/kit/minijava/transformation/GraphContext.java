package edu.kit.minijava.transformation;

import firm.Construction;

public class GraphContext {

    private ExpressionResult result = null;
    private Construction construction;
    private boolean endsOnJumpNode;

    public void setConstruction(Construction construction) {
        this.construction = construction;
    }

    public ExpressionResult getResult() {
        return this.result;
    }

    public void setResult(ExpressionResult result) {
        this.result = result;
    }

    public Construction getConstruction() {
        return this.construction;
    }

    public GraphContext() {

    }

    public GraphContext(Construction construction) {
        this.construction = construction;
    }

    public void setEndsOnJumpNode(boolean endsOnJumpNode) {
        this.endsOnJumpNode = endsOnJumpNode;
    }

    public boolean endsOnJumpNode() {
        return this.endsOnJumpNode;
    }

}
