package edu.kit.minijava.transformation;

import firm.StructType;
import firm.Type;

public class EntityContext {
    private Type type;
    private int numberOfLocalVars;
    private StructType classType;


    public void setNumberOfLocalVars(int numberOfLocalVars) {
        this.numberOfLocalVars = numberOfLocalVars;
    }

    public StructType getClassType() {
        return this.classType;
    }

    public void setClassType(StructType classType) {
        this.classType = classType;
    }


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
