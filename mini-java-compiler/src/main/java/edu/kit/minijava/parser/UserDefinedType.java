package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class UserDefinedType extends BasicType {
    public UserDefinedType(String name) {
        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    public final String name;

    @Override
    public String toString() {
        return "UserDefinedType(" + this.name + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
