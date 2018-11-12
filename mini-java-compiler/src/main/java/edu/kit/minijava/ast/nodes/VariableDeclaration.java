package edu.kit.minijava.ast.nodes;

public interface VariableDeclaration extends Declaration, ASTNode {
    String getName();
    TypeReference getType();
    boolean canBeShadowedByVariableDeclarationInNestedScope();
    boolean canBeAccessed();
}
