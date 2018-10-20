package edu.kit.minijava.parser;

public final class ModuloExpression extends Expression {
    public ModuloExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public final Expression left;
    public final Expression right;
}
