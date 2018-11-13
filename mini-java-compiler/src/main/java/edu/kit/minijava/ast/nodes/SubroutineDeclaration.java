package edu.kit.minijava.ast.nodes;

import java.util.*;

public interface SubroutineDeclaration extends Declaration {

    /** The name of the subroutine. */
    String getName();

    /** References to the types the subroutine takes as parameters. */
    List<TypeReference> getParameterTypes();

    /** A reference to the type the subroutine returns. */
    TypeReference getReturnType();
}
