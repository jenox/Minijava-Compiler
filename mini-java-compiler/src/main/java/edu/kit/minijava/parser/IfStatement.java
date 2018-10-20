package edu.kit.minijava.parser;

public final class IfStatement extends Statement {
    public IfStatement(Expression condition, Statement statementIfTrue) {
        if (condition == null) throw new IllegalArgumentException();
        if (statementIfTrue == null) throw new IllegalArgumentException();

        this.condition = condition;
        this.statementIfTrue = statementIfTrue;
    }

    public final Expression condition;
    public final Statement statementIfTrue;

    @Override
    public String toString() {
        return "IfStatement(" + this.condition + ", " + this.statementIfTrue + ")";
    }
}
