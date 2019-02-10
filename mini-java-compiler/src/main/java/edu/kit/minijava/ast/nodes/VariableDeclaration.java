package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

public interface VariableDeclaration extends Declaration, ASTNode {

    /** The name of the variable. */
    String getName();

    /**
     * The location of the declaration in the source code.
     * May return null if the declaration is not explicitly contained in the source code.
     */
    TokenLocation getLocation();

    /** A reference to the type of the variable. */
    TypeReference getType();

    /** Whether or not the variable can be shadowed by a declaration in a nested scope. */
    boolean canBeShadowedByVariableDeclarationInNestedScope();

    /** Whether or not the variable can be read from or written to. */
    boolean canBeAccessed();

    /** Whether or not the variable is in fact a constant. */
    boolean isFinal();
}
