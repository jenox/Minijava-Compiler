package edu.kit.minijava.parser;

public final class Field extends ClassMember {
    public Field(String attributeName, Type type) {
        this.attributeName = attributeName;
        this.type = type;
    }

    public final String attributeName;
    public final Type type;
}
