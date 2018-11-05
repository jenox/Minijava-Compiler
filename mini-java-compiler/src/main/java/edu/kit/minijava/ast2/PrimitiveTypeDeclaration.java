package edu.kit.minijava.ast2;

import java.util.*;

public enum PrimitiveTypeDeclaration implements BasicTypeDeclaration {
    INTEGER,
    BOOLEAN,
    VOID;

    @Override
    public List<? extends SubroutineDeclaration> getStaticMethodDeclarations() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends SubroutineDeclaration> getInstanceMethodDeclarations() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends VariableDeclaration> getFieldDeclarations() {
        return Collections.emptyList();
    }
}
