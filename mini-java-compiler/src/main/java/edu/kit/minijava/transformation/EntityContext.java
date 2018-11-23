package edu.kit.minijava.transformation;

import firm.Construction;
import firm.Graph;
import firm.Type;
import firm.nodes.Node;

public class EntityContext {

    private Type type;
    private int numberOfLocalVars;
    private Graph graph;

    public Node getResult() {
        return result;
    }

    public void setResult(Node result) {
        this.result = result;
    }

    private Node result;

    public Construction getConstruction() {
        return construction;
    }

    private Construction construction;

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

}
