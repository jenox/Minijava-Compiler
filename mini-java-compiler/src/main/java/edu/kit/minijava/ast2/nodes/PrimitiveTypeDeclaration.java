package edu.kit.minijava.ast2.nodes;

import java.util.*;

public enum PrimitiveTypeDeclaration implements BasicTypeDeclaration {
    INTEGER,
    BOOLEAN,
    VOID;

    @Override
    public List<MethodDeclaration> getMethodDeclarations() {
        return Collections.emptyList();
    }

    @Override
    public List<FieldDeclaration> getFieldDeclarations() {
        return Collections.emptyList();
    }
}
