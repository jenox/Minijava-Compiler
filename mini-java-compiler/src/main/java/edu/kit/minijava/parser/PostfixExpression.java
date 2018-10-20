package edu.kit.minijava.parser;

public final class PostfixExpression extends Expression {
    public PostfixExpression(Expression expression, PostfixOperation postfixOperation) {
        if (expression == null) { throw new IllegalArgumentException(); }
        if (postfixOperation == null) { throw new IllegalArgumentException(); }

        this.expression = expression;
        this.postfixOperation = postfixOperation;
    }

    public final Expression expression;
    public final PostfixOperation postfixOperation;

    @Override
    public String toString() {
        return "PostfixExpression(" + this.expression + ", " + this.postfixOperation + ")";
    }
}
