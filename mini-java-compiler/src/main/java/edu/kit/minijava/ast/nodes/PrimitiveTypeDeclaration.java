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

    public boolean canBeReferencedByUser() {
        return this.canBeReferencedByUser;
    }
}
