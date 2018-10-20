package edu.kit.minijava.parser;

public final class LessThanExpression extends Expression {
    public LessThanExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
