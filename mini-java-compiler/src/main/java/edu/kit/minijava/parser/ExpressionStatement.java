package edu.kit.minijava.parser;

public final class ExpressionStatement extends Statement {
    public ExpressionStatement(Expression expression) {
        if (expression == null) throw new IllegalArgumentException();

        this.expression = expression;
    }

    public final Expression expression;

    @Override
    public String toString() {
        return "ExpressionStatement(" + this.expression + ")";
    }
}
