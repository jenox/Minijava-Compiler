package edu.kit.minijava.parser;

public final class WhileStatement extends Statement {
    public WhileStatement(Expression condition, Statement statement) {
        if (condition == null) throw new IllegalArgumentException();
        if (statement == null) throw new IllegalArgumentException();

        this.condition = condition;
        this.statement = statement;
    }

    public final Expression condition;
    public final Statement statement;

    @Override
    public String toString() {
        return "WhileStatement(" + this.condition + ", " + this.statement + ")";
    }
}
