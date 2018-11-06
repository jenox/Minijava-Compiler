package edu.kit.minijava.ast2.nodes;

import java.util.*;

public interface BasicTypeDeclaration {
    List<MethodDeclaration> getMethodDeclarations();
    List<FieldDeclaration> getFieldDeclarations();
}
