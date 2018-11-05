package edu.kit.minijava.ast;

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
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
