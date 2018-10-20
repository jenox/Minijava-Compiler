package edu.kit.minijava.parser;

public final class LocalVariableDeclarationAndInitializationStatement extends BlockStatement {
    public LocalVariableDeclarationAndInitializationStatement(Type type, String name, Expression value) {
        if (type == null) { throw new IllegalArgumentException(); }
        if (name == null) { throw new IllegalArgumentException(); }
        if (value == null) { throw new IllegalArgumentException(); }

        this.type = type;
        this.name = name;
        this.value = value;
    }

    public final Type type;
    public final String name;
    public final Expression value;

    @Override
    public String toString() {
        return "LocalVariableDeclarationAndInitializationStatement(" + this.type + ", " + this.name + ", " + this.value + ")";
    }
}
