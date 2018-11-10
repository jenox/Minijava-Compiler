package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

public interface VariableDeclaration extends Declaration {
    String getName();
    TypeReference getType();
}
