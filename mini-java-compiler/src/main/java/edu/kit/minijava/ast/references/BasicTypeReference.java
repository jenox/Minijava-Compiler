package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

public final class BasicTypeReference extends AbstractReference<BasicTypeDeclaration> {
    public BasicTypeReference(String name) {
        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    public BasicTypeReference(String name, PrimitiveTypeDeclaration declaration) {
        if (name == null) throw new IllegalArgumentException();
        if (declaration == null) throw new IllegalArgumentException();

        this.name = name;

        this.resolveTo(declaration);
    }

    public BasicTypeReference(PrimitiveTypeDeclaration declaration) {
        if (declaration == null) throw new IllegalArgumentException();

        this.name = null;

        this.resolveTo(declaration);
    }

    public final String name;
}
