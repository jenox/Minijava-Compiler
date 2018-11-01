package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class BooleanType extends BasicType {
    @Override
    public String toString() {
        return "BooleanType";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
