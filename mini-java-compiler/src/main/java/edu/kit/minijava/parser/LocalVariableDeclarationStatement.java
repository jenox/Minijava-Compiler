package edu.kit.minijava.parser;

public final class LocalVariableDeclarationStatement extends BlockStatement {
    public LocalVariableDeclarationStatement(Type type, String name, Expression value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public final Type type;
    public final String name;
    public final Expression value; // optional
}
