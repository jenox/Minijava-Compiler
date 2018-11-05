package edu.kit.minijava.ast;

public final class EmptyStatement extends Statement {
    @Override
    public String toString() {
        return "EmptyStatement";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
