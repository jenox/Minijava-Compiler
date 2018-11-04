package edu.kit.minijava.parser;

public final class WhileStatement extends Statement {
    public WhileStatement(Expression condition, Statement statementWhileTrue) {
        if (condition == null) throw new IllegalArgumentException();
        if (statementWhileTrue == null) throw new IllegalArgumentException();

        this.condition = condition;
        this.statementWhileTrue = statementWhileTrue;
    }

    public final Expression condition;
    public final Statement statementWhileTrue;

    @Override
    public String toString() {
        return "WhileStatement(" + this.condition + ", " + this.statementWhileTrue + ")";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
