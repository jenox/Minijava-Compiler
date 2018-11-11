package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.TypeReference;

import java.util.List;

public interface SubroutineDeclaration extends Declaration {
    List<TypeReference> getParameterTypes();

    TypeReference getReturnType();
}
