package edu.kit.minijava.ast;

public final class VoidType extends BasicType {
    @Override
    public String toString() {
        return "VoidType";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
