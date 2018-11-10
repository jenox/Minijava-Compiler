package edu.kit.minijava.ast.nodes;

import java.util.*;

public interface BasicTypeDeclaration extends Declaration {
    String getName();
    List<MethodDeclaration> getMethodDeclarations();
    List<FieldDeclaration> getFieldDeclarations();
}
