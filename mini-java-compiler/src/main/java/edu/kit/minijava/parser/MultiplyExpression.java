package edu.kit.minijava.parser;

public final class MultiplyExpression extends Expression {
    public MultiplyExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
