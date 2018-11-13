package edu.kit.minijava.ast.nodes;

public interface ASTNode {
    <T> void accept(ASTVisitor<T> visitor, T context);

    default <T> void accept(ASTVisitor<T> visitor) {
        this.accept(visitor, null);
    }
}
