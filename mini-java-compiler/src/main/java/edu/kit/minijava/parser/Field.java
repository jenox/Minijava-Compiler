package edu.kit.minijava.parser;

public final class Field extends ClassMember {
    public Field(String attributeName, Type type) {
        if (attributeName == null) throw new IllegalArgumentException();
        if (type == null) throw new IllegalArgumentException();

        this.attributeName = attributeName;
        this.type = type;
    }

    public final String attributeName;
    public final Type type;

    @Override
    public String toString() {
        return "Field(" + this.attributeName + ", " + this.type + ")";
    }
}
