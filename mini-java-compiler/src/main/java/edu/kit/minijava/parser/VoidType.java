package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class VoidType extends BasicType {
    @Override
    public String toString() {
        return "VoidType";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
