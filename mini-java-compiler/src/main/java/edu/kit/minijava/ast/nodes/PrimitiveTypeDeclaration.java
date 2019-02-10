package edu.kit.minijava.ast.nodes;

import java.util.*;

public enum PrimitiveTypeDeclaration implements BasicTypeDeclaration {
    INTEGER("int", true),
    BOOLEAN("boolean", true),
    VOID("void", true),
    STRING("String", false);

    PrimitiveTypeDeclaration(String name, boolean canBeReferencedByUser) {
        this.name = name;
        this.canBeReferencedByUser = canBeReferencedByUser;
    }

    private final String name;
    private final boolean canBeReferencedByUser;

    @Override
    public String getName() {
        return this.name;
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
    public boolean isClassDeclaration() {
        return false;
    }

    public boolean canBeReferencedByUser() {
        return this.canBeReferencedByUser;
    }

    @Override
    public String toStringForDumpingAST() {
        switch (this) {
            case INTEGER: return "int";
            case BOOLEAN: return "boolean";
            case VOID: return "void";
            case STRING: return "String";
            default: throw new AssertionError();
        }
    }
}
