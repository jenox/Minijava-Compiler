package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class IdentifierExpression extends Expression {
    public IdentifierExpression(String identifier) {
        if (identifier == null) throw new IllegalArgumentException();

        this.identifier = identifier;
    }

    public final String identifier;

    @Override
    public String toString() {
        return "IdentifierExpression(" + this.identifier + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
