package edu.kit.minijava.parser;

public final class NegateExpression extends Expression {
    public NegateExpression(Expression other) {
        this.other = other;
    }

    public final Expression other;
}
