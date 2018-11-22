package edu.kit.minijava.transformation;

import firm.Type;

public class EntityContext {

    private Type type;
    private int numberOfLocalVars;

    public EntityContext() {
        this.numberOfLocalVars = 0;
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
