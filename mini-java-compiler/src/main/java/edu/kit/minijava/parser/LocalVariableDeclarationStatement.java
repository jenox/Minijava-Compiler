package edu.kit.minijava.parser;

public final class LocalVariableDeclarationStatement extends BlockStatement {
    public LocalVariableDeclarationStatement(Type type, String name, Expression value) {
        if (type == null) { throw new IllegalArgumentException(); }
        if (name == null) { throw new IllegalArgumentException(); }

        this.type = type;
        this.name = name;
    }

    public final Type type;
    public final String name;

    @Override
    public String toString() {
        return "LocalVariableDeclarationStatement(" + this.type + ", " + this.name + ")";
    }
}
