package edu.kit.minijava.parser;

public final class NullLiteral extends Expression {
    @Override
    public String toString() {
        return "NullLiteral";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
