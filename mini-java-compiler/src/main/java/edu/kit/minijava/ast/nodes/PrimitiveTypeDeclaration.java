package edu.kit.minijava.ast.nodes;

import java.util.Collections;
import java.util.List;

public enum PrimitiveTypeDeclaration implements BasicTypeDeclaration {

    INTEGER("int"),
    BOOLEAN("boolean"),
    VOID("void");

    private String typeName;

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
