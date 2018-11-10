package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

import java.util.*;

public interface SubroutineDeclaration extends Declaration {
    String getName();
    List<TypeReference> getParameterTypes();
    TypeReference getReturnType();
}
