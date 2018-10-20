package edu.kit.minijava.parser;

public final class ReturnStatement extends Statement {
    public ReturnStatement(Expression returnValue) {
        if (returnValue == null) { throw new IllegalArgumentException(); }

        this.returnValue = returnValue;
    }

    public final Expression returnValue; // optional

    @Override
    public String toString() {
        return "ReturnStatement(" + this.returnValue + ")";
    }
}
