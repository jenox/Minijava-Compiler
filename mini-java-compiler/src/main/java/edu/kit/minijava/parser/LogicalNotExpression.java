package edu.kit.minijava.parser;

public final class LogicalNotExpression extends Expression {
    public LogicalNotExpression(Expression other) {
        this.other = other;
    }

    public final Expression other;
}
