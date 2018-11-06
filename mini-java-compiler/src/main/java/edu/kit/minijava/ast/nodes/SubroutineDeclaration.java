package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

import java.util.*;

public interface SubroutineDeclaration {
    List<TypeReference> getParameterTypes();
    TypeReference getReturnType();
}
