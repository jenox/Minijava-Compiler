package edu.kit.minijava.parser;

public final class ReturnValueStatement extends Statement {
    public ReturnValueStatement(Expression returnValue) {
        if (returnValue == null) { throw new IllegalArgumentException(); }

        this.returnValue = returnValue;
    }

    public final Expression returnValue;

    @Override
    public String toString() {
        return "ReturnValueStatement(" + this.returnValue + ")";
    }
}
