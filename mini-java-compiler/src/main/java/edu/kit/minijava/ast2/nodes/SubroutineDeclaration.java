package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

import java.util.*;

public interface SubroutineDeclaration {
    List<TypeReference> getParameterTypes();
    TypeReference getReturnType();
}
