package edu.kit.minijava.transformation;

import firm.Construction;
import firm.Graph;
import firm.StructType;
import firm.Type;
import firm.nodes.Node;

public class EntityContext {

    private Type type;
    private int numberOfLocalVars;
    private Graph graph;
    private Node result = null;
    private Construction construction;
    private StructType classType;

    public StructType getClassType() {
        return classType;
    }

    public void setClassType(StructType classType) {
        this.classType = classType;
    }




    public Node getResult() {
        return result;
    }

    public void setResult(Node result) {
        this.result = result;
    }


    public Construction getConstruction() {
        return construction;
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

}
