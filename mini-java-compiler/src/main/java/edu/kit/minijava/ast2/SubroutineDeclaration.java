package edu.kit.minijava.ast2;

import java.util.*;

public interface SubroutineDeclaration {
    List<? extends TypeReference> getParameterTypes();
    TypeReference getReturnType();
}
