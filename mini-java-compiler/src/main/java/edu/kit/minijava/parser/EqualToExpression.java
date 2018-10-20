package edu.kit.minijava.parser;

public final class EqualToExpression extends Expression {
    public EqualToExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
