package edu.kit.minijava.parser;

public final class IdentifierExpression extends Expression {
    public IdentifierExpression(String identifier) {
        this.identifier = identifier;
    }

    public final String identifier;
}
