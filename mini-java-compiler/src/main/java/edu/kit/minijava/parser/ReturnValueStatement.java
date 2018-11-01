package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class ReturnValueStatement extends Statement {
    public ReturnValueStatement(Expression returnValue) {
        if (returnValue == null) throw new IllegalArgumentException();

        this.returnValue = returnValue;
    }

    public final Expression returnValue;

    @Override
    public String toString() {
        return "ReturnValueStatement(" + this.returnValue + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
