package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

public final class TypeReference extends AbstractReference<BasicTypeDeclaration> {
    public TypeReference() {
        this.name = null;
        this.numberOfDimensions = 0;
    }

    public TypeReference(String name, int numberOfDimensions) {
        if (name == null) throw new IllegalArgumentException();

        this.name = name;
        this.numberOfDimensions = numberOfDimensions;
    }

    public TypeReference(String name, PrimitiveTypeDeclaration declaration, int numberOfDimensions) {
        if (name == null) throw new IllegalArgumentException();
        if (declaration == null) throw new IllegalArgumentException();

        this.name = name;
        this.numberOfDimensions = numberOfDimensions;

        this.resolveTo(declaration);
    }

    public TypeReference(BasicTypeReference reference, int numberOfDimensions) {
        if (reference == null) throw new IllegalArgumentException();

        this.name = reference.getName();
        this.numberOfDimensions = numberOfDimensions;

        if (reference.isResolved()) {
            this.resolveTo(reference.getDeclaration());
        }
    }

    private final String name; // nullable
    private final int numberOfDimensions;

    public String getName() {
        return this.name;
    }

    public int getNumberOfDimensions() {
        return this.numberOfDimensions;
    }
}
