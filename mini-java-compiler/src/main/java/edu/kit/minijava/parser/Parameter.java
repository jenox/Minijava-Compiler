package edu.kit.minijava.parser;

public final class Parameter {
    public Parameter(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public final Type type;
    public final String name;
}
