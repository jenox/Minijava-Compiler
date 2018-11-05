package edu.kit.minijava.ast;

public final class ArrayAccess extends PostfixOperation {
    public ArrayAccess(Expression index) {
        if (index == null) throw new IllegalArgumentException();

        this.index = index;
    }

    public final Expression index;

    @Override
    public String toString() {
        return "ArrayAccess(" + this.index + ")";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
