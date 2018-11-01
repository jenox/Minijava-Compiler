package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class ExpressionStatement extends Statement {
    public ExpressionStatement(Expression expression) {
        if (expression == null) throw new IllegalArgumentException();

        this.expression = expression;
    }

    public final Expression expression;

    @Override
    public String toString() {
        return "ExpressionStatement(" + this.expression + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
