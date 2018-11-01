package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class NullLiteral extends Expression {
    @Override
    public String toString() {
        return "NullLiteral";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
