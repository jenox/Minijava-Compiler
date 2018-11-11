package edu.kit.minijava.ast.nodes;

import java.util.*;

public enum PrimitiveTypeDeclaration implements BasicTypeDeclaration {
    INTEGER,
    BOOLEAN,
    VOID;

    @Override
    public String getName() {
        switch (this) {
            case INTEGER: return "int";
            case BOOLEAN: return "boolean";
            case VOID: return "void";
            default: throw new AssertionError();
        }
    }

    @Override
    public List<MethodDeclaration> getMethodDeclarations() {
        return Collections.emptyList();
    }

    @Override
    public List<FieldDeclaration> getFieldDeclarations() {
        return Collections.emptyList();
    }
}
