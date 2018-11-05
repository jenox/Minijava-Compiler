package edu.kit.minijava.ast;

public final class ReturnNoValueStatement extends Statement {
    public ReturnNoValueStatement() {
    }

    @Override
    public String toString() {
        return "ReturnNoValueStatement";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
