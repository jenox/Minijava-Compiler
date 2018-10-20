package edu.kit.minijava.parser;

public final class WhileStatement extends Statement {
    public WhileStatement(Expression condition, Statement statement) {
        this.condition = condition;
        this.statement = statement;
    }

    public final Expression condition;
    public final Statement statement;
}
