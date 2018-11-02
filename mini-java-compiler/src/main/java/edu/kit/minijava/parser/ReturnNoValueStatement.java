package edu.kit.minijava.parser;

public final class ReturnNoValueStatement extends Statement {
    public ReturnNoValueStatement() {
    }

    @Override
    public String toString() {
        return "ReturnNoValueStatement";
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
