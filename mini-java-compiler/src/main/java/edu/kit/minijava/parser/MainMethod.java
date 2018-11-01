package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class MainMethod extends ClassMember {
    public MainMethod(String name, String argumentsParameterName, Block body) {
        if (name == null) throw new IllegalArgumentException();
        if (argumentsParameterName == null) throw new IllegalArgumentException();
        if (body == null) throw new IllegalArgumentException();

        this.name = name;
        this.argumentsParameterName = argumentsParameterName;
        this.body = body;
    }

    public final String name;
    public final String argumentsParameterName;
    public final Block body;

    @Override
    public String toString() {
        return "MainMethod(" + this.name + ", " + this.argumentsParameterName + ", " + this.body + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
