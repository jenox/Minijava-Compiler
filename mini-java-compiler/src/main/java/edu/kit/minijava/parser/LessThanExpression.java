package edu.kit.minijava.parser;

public final class LessThanExpression extends Expression {
    public LessThanExpression(Expression left, Expression right) {
        if (left == null) throw new IllegalArgumentException();
        if (right == null) throw new IllegalArgumentException();

        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;

    @Override
    public String toString() {
        return "LessThanExpression(" + this.left + ", " + this.right + ")";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
