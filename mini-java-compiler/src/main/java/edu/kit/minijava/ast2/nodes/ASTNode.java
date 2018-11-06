package edu.kit.minijava.ast2.nodes;

public abstract class ASTNode {
    public abstract <T> void accept(ASTVisitor<T> visitor, T context);
}
