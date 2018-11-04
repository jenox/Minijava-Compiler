package edu.kit.minijava.parser;

public final class IntegerType extends BasicType {
    @Override
    public String toString() {
        return "IntegerType";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
