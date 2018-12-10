package edu.kit.minijava.transformation;

import edu.kit.minijava.ast.nodes.Declaration;
import firm.Construction;
import firm.StructType;
import firm.Type;

public class EntityContext {

    private Type type;
    private int numberOfLocalVars;
    private ExpressionResult result = null;
    private Construction construction;
    private StructType classType;
    private boolean isLeftSideOfAssignment;
    private boolean isCalledFromMain;
    private boolean endsOnJumpNode;

    public boolean isCalledFromMain() {
        return this.isCalledFromMain;
    }

    public void setCalledFromMain(boolean calledFromMain) {
        this.isCalledFromMain = calledFromMain;
    }

    public boolean isLeftSideOfAssignment() {
        return this.isLeftSideOfAssignment;
    }

    public void setLeftSideOfAssignment(boolean leftSideOfAssignment) {
        this.isLeftSideOfAssignment = leftSideOfAssignment;
    }

    public void setNumberOfLocalVars(int numberOfLocalVars) {
        this.numberOfLocalVars = numberOfLocalVars;
    }

    public StructType getClassType() {
        return this.classType;
    }

    public void setClassType(StructType classType) {
        this.classType = classType;
    }

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

    public EntityContext() {
        this.numberOfLocalVars = 0;
    }

    public EntityContext(Construction construction) {
        this.numberOfLocalVars = 0;
        this.construction = construction;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void incrementLocalVarCount() {
        this.numberOfLocalVars++;
    }

    public int getNumberOfLocalVars() {
        return this.numberOfLocalVars;
    }

    public void setEndsOnJumpNode(boolean endsOnJumpNode) {
        this.endsOnJumpNode = endsOnJumpNode;
    }

    public boolean endsOnJumpNode() {
        return this.endsOnJumpNode;
    }

}
