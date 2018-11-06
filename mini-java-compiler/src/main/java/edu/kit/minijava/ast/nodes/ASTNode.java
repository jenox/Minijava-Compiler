package edu.kit.minijava.ast.nodes;

public abstract class ASTNode {
    public abstract <T> void accept(ASTVisitor<T> visitor, T context);
}
