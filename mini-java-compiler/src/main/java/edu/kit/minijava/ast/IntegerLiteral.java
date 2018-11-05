package edu.kit.minijava.ast;

public final class IntegerLiteral extends Expression {
    public IntegerLiteral(String value) {
        this.value = value;
    }

    public final String value;

    @Override
    public String toString() {
        return "IntegerLiteral(" + this.value + ")";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}