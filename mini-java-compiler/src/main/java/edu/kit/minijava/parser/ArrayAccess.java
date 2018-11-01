package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class ArrayAccess extends PostfixOperation {
    public ArrayAccess(Expression index) {
        if (index == null) throw new IllegalArgumentException();

        this.index = index;
    }

    public final Expression index;

    @Override
    public String toString() {
        return "ArrayAccess(" + this.index + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
