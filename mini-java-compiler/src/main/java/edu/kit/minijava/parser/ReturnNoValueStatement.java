package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class ReturnNoValueStatement extends Statement {
    public ReturnNoValueStatement() {
    }

    @Override
    public String toString() {
        return "ReturnNoValueStatement";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
