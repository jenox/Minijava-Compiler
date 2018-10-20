package edu.kit.minijava.parser;

public final class GreaterThanExpression extends Expression {
    public GreaterThanExpression(Expression left, Expression right) {
        if (left == null) { throw new IllegalArgumentException(); }
        if (right == null) { throw new IllegalArgumentException(); }

        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;

    @Override
    public String toString() {
        return "GreaterThanExpression(" + this.left + ", " + this.right + ")";
    }
}
