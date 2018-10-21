package edu.kit.minijava.parser;

public class IfElseStatement extends Statement {
    public IfElseStatement(Expression condition, Statement statementIfTrue, Statement statementIfFalse) {
        if (condition == null) throw new IllegalArgumentException();
        if (statementIfTrue == null) throw new IllegalArgumentException();
        if (statementIfFalse == null) throw new IllegalArgumentException();

        this.condition = condition;
        this.statementIfTrue = statementIfTrue;
        this.statementIfFalse = statementIfFalse;
    }

    public final Expression condition;
    public final Statement statementIfTrue;
    public final Statement statementIfFalse;

    @Override
    public String toString() {
        return "IfStatement(" + this.condition + ", " + this.statementIfTrue + ", " + this.statementIfFalse + ")";
    }
}
