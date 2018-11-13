package edu.kit.minijava.ast.nodes;

import java.util.*;

public interface BasicTypeDeclaration extends Declaration {

    /** The name of the basic type. */
    String getName();

    /** The (instance) methods declared by the type. */
    List<MethodDeclaration> getMethodDeclarations();

    /** The fields declared by the type. */
    List<FieldDeclaration> getFieldDeclarations();
}
