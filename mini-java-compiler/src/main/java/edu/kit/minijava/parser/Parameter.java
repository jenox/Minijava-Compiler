package edu.kit.minijava.parser;

public final class Parameter {
    public Parameter(Type type, String name) {
        if (type == null) { throw new IllegalArgumentException(); }
        if (name == null) { throw new IllegalArgumentException(); }

        this.type = type;
        this.name = name;
    }

    public final Type type;
    public final String name;

    @Override
    public String toString() {
        return "Parameter(" + this.type + ", " + this.name + ")";
    }
}
