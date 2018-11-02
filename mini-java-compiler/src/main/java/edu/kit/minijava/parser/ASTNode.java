package edu.kit.minijava.parser;

public abstract class ASTNode {
    
    public abstract void accept(NodeVisitor visitor);
}
