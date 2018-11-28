package edu.kit.minijava.transformation;

import edu.kit.minijava.ast.nodes.Declaration;
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

    public Declaration getDecl() {
        return this.decl;
    }

    public void setDecl(Declaration decl) {
        this.decl = decl;
    }

    private Declaration decl;

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

    public Node getResult() {
        return this.result;
    }

    public void setResult(Node result) {
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

}
