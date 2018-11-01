package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class IntegerType extends BasicType {
    @Override
    public String toString() {
        return "IntegerType";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
