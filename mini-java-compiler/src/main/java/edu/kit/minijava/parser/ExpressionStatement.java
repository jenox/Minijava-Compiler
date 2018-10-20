package edu.kit.minijava.parser;

public final class ExpressionStatement extends Statement {
    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    public final Expression expression;
}
