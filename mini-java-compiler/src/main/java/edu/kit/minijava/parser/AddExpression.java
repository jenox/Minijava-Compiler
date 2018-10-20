package edu.kit.minijava.parser;

public final class AddExpression extends Expression {
    public AddExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
