package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.TypeReference;

public interface VariableDeclaration extends Declaration {
    TypeReference getType();

    boolean canDeclarationBeShadowed();
}
