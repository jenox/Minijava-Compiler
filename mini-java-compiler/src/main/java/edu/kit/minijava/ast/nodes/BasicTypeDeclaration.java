package edu.kit.minijava.ast.nodes;

import java.util.List;

public interface BasicTypeDeclaration extends Declaration {
    List<MethodDeclaration> getMethodDeclarations();

    List<FieldDeclaration> getFieldDeclarations();
}
