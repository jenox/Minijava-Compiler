package edu.kit.minijava.ast2.nodes;

import java.util.*;

public interface BasicTypeDeclaration {
    List<MethodDeclaration> getStaticMethodDeclarations();
    List<MethodDeclaration> getInstanceMethodDeclarations();
    List<FieldDeclaration> getFieldDeclarations();
}
