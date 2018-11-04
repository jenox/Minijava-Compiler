package edu.kit.minijava.ast;

public final class IdentifierExpression extends Expression {
    public IdentifierExpression(String identifier) {
        if (identifier == null) throw new IllegalArgumentException();

        this.identifier = identifier;
    }

    public final String identifier;

    @Override
    public String toString() {
        return "IdentifierExpression(" + this.identifier + ")";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
