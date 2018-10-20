package edu.kit.minijava.parser;

public final class DivideExpression extends Expression {
    public DivideExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
