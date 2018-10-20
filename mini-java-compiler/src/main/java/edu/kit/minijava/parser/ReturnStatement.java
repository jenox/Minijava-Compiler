package edu.kit.minijava.parser;

public final class ReturnStatement extends Statement {
    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    public final Expression returnValue; // optional
}
