package edu.kit.minijava.ast.nodes;

import java.util.*;

public interface BasicTypeDeclaration {
    List<MethodDeclaration> getMethodDeclarations();
    List<FieldDeclaration> getFieldDeclarations();
}
