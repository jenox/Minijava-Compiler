package edu.kit.minijava.parser;

public final class GreaterThanOrEqualToExpression extends Expression {
    public GreaterThanOrEqualToExpression(Expression left, Expression right) {
        if (left == null) throw new IllegalArgumentException();
        if (right == null) throw new IllegalArgumentException();

        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;

    @Override
    public String toString() {
        return "GreaterThanOrEqualToExpression(" + this.left + ", " + this.right + ")";
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
