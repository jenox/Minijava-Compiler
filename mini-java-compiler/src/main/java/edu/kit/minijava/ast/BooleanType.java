package edu.kit.minijava.ast;

public final class BooleanType extends BasicType {
    @Override
    public String toString() {
        return "BooleanType";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
