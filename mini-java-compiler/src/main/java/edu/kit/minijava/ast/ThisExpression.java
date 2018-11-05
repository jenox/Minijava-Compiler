package edu.kit.minijava.ast;

public final class ThisExpression extends Expression {
    @Override
    public String toString() {
        return "ThisExpression";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
