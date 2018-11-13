package edu.kit.minijava.ast.nodes;

public interface VariableDeclaration extends Declaration, ASTNode {

    /** The name of the variable. */
    String getName();

    /** A reference to the type of the variable. */
    TypeReference getType();

    /** Whether or not the variable can be shadowed by a declaration in a nested scope. */
    boolean canBeShadowedByVariableDeclarationInNestedScope();

    /** Whether or not the variable can be read from or written to. */
    boolean canBeAccessed();
}
