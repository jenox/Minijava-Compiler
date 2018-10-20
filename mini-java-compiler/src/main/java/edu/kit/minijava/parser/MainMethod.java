package edu.kit.minijava.parser;

public final class MainMethod extends ClassMember {
    public MainMethod(String name, String argumentsParameterName, MethodRest rest, Block body) {
        this.name = name;
        this.argumentsParameterName = argumentsParameterName;
        this.rest = rest;
        this.body = body;
    }

    public final String name;
    public final String argumentsParameterName;
    public final MethodRest rest; // nullable
    public final Block body;
}
