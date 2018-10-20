package edu.kit.minijava.parser;

public final class GreaterThanOrEqualToExpression extends Expression {
    public GreaterThanOrEqualToExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
