package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class ThisExpression extends Expression {
    @Override
    public String toString() {
        return "ThisExpression";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
