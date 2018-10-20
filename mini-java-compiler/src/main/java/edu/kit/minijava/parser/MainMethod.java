package edu.kit.minijava.parser;

public final class MainMethod extends ClassMember {
    public MainMethod(String name, String argumentsParameterName, MethodRest rest, Block body) {
        if (name == null) { throw new IllegalArgumentException(); }
        if (argumentsParameterName == null) { throw new IllegalArgumentException(); }
        if (body == null) { throw new IllegalArgumentException(); }

        this.name = name;
        this.argumentsParameterName = argumentsParameterName;
        this.rest = rest;
        this.body = body;
    }

    public final String name;
    public final String argumentsParameterName;
    public final MethodRest rest; // nullable
    public final Block body;

    @Override
    public String toString() {
        return "MainMethod(" + this.name + ", " + this.argumentsParameterName + ", " + this.body + ")";
    }
}
