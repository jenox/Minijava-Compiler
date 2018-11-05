package edu.kit.minijava.ast;

public final class IntegerType extends BasicType {
    @Override
    public String toString() {
        return "IntegerType";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
