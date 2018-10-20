package edu.kit.minijava.parser;

public final class NewObjectExpression extends Expression {
    public NewObjectExpression(String className) {
        this.className = className;
    }

    public final String className;
}
