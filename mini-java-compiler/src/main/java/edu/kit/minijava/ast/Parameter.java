package edu.kit.minijava.ast;

public final class Parameter extends ASTNode {
    public Parameter(Type type, String name) {
        if (type == null) throw new IllegalArgumentException();
        if (name == null) throw new IllegalArgumentException();

        this.type = type;
        this.name = name;
    }

    public final Type type;
    public final String name;

    @Override
    public String toString() {
        return "Parameter(" + this.type + ", " + this.name + ")";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
