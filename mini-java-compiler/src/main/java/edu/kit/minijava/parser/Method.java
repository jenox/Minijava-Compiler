package edu.kit.minijava.parser;

import java.util.Collections;
import java.util.List;

public final class Method extends ClassMember {
    public Method(Type returnType, String name, List<Parameter> parameters, Block body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = Collections.unmodifiableList(parameters);
        this.body = body;
    }

    public final Type returnType;
    public final String name;
    public final List<Parameter> parameters;
    public final Block body;

    @Override
    public String toString() {
        return "Method(" + this.returnType + ", " + this.name + ", " + this.parameters + ", " + this.body + ")";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMethod() {
        return true;
    }

}
