package edu.kit.minijava.parser;

public final class NotEqualToExpression extends Expression {
    public NotEqualToExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
