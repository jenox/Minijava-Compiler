package edu.kit.minijava.parser;

public final class VoidType extends BasicType {
    @Override
    public String toString() {
        return "VoidType";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
