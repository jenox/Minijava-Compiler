package edu.kit.minijava.parser;

public final class IfStatement extends Statement {
    public IfStatement(Expression condition, Statement statement, Statement otherwiseStatement) {
        this.condition = condition;
        this.statement = statement;
        this.otherwiseStatement = otherwiseStatement;
    }

    public final Expression condition;
    public final Statement statement;
    public final Statement otherwiseStatement; // optional
}
