package edu.kit.minijava.parser;

public final class PostfixExpression extends Expression {
    public PostfixExpression(Expression expression, PostfixOperation postfixOperation) {
        this.expression = expression;
        this.postfixOperation = postfixOperation;
    }

    public final Expression expression;
    public final PostfixOperation postfixOperation;
}
