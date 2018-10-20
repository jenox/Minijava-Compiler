package edu.kit.minijava.parser;

public final class BooleanLiteral extends Expression {
    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public final boolean value;
}
