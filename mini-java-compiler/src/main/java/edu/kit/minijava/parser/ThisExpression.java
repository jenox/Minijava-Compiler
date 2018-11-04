package edu.kit.minijava.parser;

public final class ThisExpression extends Expression {
    @Override
    public String toString() {
        return "ThisExpression";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
