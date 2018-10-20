package edu.kit.minijava.parser;

public final class GreaterThanExpression extends Expression {
    public GreaterThanExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
