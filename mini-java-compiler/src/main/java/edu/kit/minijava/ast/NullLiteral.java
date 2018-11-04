package edu.kit.minijava.ast;

public final class NullLiteral extends Expression {
    @Override
    public String toString() {
        return "NullLiteral";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
