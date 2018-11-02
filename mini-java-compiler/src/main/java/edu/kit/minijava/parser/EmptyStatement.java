package edu.kit.minijava.parser;

public final class EmptyStatement extends Statement {
    @Override
    public String toString() {
        return "EmptyStatement";
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
