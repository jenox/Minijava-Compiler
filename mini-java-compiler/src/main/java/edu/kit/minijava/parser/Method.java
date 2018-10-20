package edu.kit.minijava.parser;

import java.util.*;

public final class Method extends ClassMember {
    public Method(Type returnType, String name, List<Parameter> parameters, MethodRest rest, Block body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = Collections.unmodifiableList(parameters);
        this.rest = rest;
        this.body = body;
    }

    public final Type returnType;
    public final String name;
    public final List<Parameter> parameters;
    public final MethodRest rest; // nullable
    public final Block body;
}
