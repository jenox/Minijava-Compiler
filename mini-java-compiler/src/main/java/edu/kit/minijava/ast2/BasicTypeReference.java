package edu.kit.minijava.ast2;

public final class BasicTypeReference extends AbstractReference<BasicTypeDeclaration> {
    public BasicTypeReference(String name) {
        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    public BasicTypeReference(PrimitiveTypeDeclaration declaration) {
        this.name = null;

        this.resolveTo(declaration);
    }

    public final String name;
}
