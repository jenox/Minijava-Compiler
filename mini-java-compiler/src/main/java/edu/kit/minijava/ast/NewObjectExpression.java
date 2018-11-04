package edu.kit.minijava.ast;

public final class NewObjectExpression extends Expression {
    public NewObjectExpression(String className) {
        if (className == null) throw new IllegalArgumentException();

        this.className = className;
    }

    public final String className;

    @Override
    public String toString() {
        return "NewObjectExpression(" + this.className + ")";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
