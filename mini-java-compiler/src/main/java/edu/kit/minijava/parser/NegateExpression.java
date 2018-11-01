package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class NegateExpression extends Expression {
    public NegateExpression(Expression other) {
        if (other == null) throw new IllegalArgumentException();

        this.other = other;
    }

    public final Expression other;

    @Override
    public String toString() {
        return "NegateExpression(" + this.other + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
