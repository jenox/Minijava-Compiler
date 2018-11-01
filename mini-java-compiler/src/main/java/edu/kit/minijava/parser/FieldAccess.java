package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class FieldAccess extends PostfixOperation {
    public FieldAccess(String fieldName) {
        if (fieldName == null) throw new IllegalArgumentException();

        this.fieldName = fieldName;
    }

    public final String fieldName;

    @Override
    public String toString() {
        return "FieldAccess(" + this.fieldName + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
