package edu.kit.minijava.ast2.nodes;

public abstract class ASTNode {
    public abstract void accept(ASTVisitor visitor);
}
