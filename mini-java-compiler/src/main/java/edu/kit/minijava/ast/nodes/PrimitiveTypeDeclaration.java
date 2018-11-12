package edu.kit.minijava.ast.nodes;

import java.util.*;

public enum PrimitiveTypeDeclaration implements BasicTypeDeclaration {

    INTEGER("int"),
    BOOLEAN("boolean"),
    VOID("void");

    private final String typeName;

    PrimitiveTypeDeclaration(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public List<MethodDeclaration> getMethodDeclarations() {
        return Collections.emptyList();
    }

    @Override
    public List<FieldDeclaration> getFieldDeclarations() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return this.typeName;
    }
}
