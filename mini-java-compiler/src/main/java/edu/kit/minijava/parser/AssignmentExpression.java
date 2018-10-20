package edu.kit.minijava.parser;

public final class AssignmentExpression extends Expression {
    public AssignmentExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
