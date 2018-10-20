package edu.kit.minijava.parser;

public final class SubtractExpression extends Expression {
    public SubtractExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}