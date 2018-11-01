package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class LogicalNotExpression extends Expression {
    public LogicalNotExpression(Expression other) {
        if (other == null) throw new IllegalArgumentException();

        this.other = other;
    }

    public final Expression other;

    @Override
    public String toString() {
        return "LogicalNotExpression(" + this.other + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
