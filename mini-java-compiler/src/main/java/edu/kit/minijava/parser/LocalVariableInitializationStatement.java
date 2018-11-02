package edu.kit.minijava.parser;

public final class LocalVariableInitializationStatement extends BlockStatement {
    public LocalVariableInitializationStatement(Type type, String name, Expression value) {
        if (type == null) throw new IllegalArgumentException();
        if (name == null) throw new IllegalArgumentException();
        if (value == null) throw new IllegalArgumentException();

        this.type = type;
        this.name = name;
        this.value = value;
    }

    public final Type type;
    public final String name;
    public final Expression value;

    @Override
    public String toString() {
        return "LocalVariableInitializationStatement(" + this.type + ", " + this.name + ", " + this.value + ")";
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
