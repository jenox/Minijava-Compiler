package edu.kit.minijava.parser;

public final class LessThanOrEqualToExpression extends Expression {
    public LessThanOrEqualToExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
