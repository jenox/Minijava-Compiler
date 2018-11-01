package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class EmptyStatement extends Statement {
    @Override
    public String toString() {
        return "EmptyStatement";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
