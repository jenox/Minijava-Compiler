package edu.kit.minijava.parser;

public final class LogicalOrExpression extends Expression {
    public LogicalOrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
