package edu.kit.minijava.ast2;

public final class TypeReference extends AbstractReference<BasicTypeDeclaration> {
    public TypeReference(BasicTypeReference reference, int numberOfDimensions) {
        this.name = reference.name;
        this.numberOfDimensions = numberOfDimensions;

        if (reference.isResolved()) {
            this.resolveTo(reference.getDeclaration());
        }
    }

    public final String name;
    public final int numberOfDimensions;
}
