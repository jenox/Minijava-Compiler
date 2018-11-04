package edu.kit.minijava.parser;

public final class BooleanType extends BasicType {
    @Override
    public String toString() {
        return "BooleanType";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
