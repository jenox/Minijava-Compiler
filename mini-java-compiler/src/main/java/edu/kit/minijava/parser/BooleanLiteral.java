package edu.kit.minijava.parser;

public final class BooleanLiteral extends Expression {
    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public final boolean value;

    @Override
    public String toString() {
        return "BooleanLiteral(" + this.value + ")";
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
