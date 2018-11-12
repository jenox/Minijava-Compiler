package edu.kit.minijava.ast.references;

public final class TypeReference {
    public TypeReference(BasicTypeReference basicTypeReference, int numberOfDimensions) {
        this.basicTypeReference = basicTypeReference;
        this.numberOfDimensions = numberOfDimensions;
    }

    private final BasicTypeReference basicTypeReference;
    private final int numberOfDimensions;

    public final BasicTypeReference getBasicTypeReference() {
        return this.basicTypeReference;
    }

    public final int getNumberOfDimensions() {
        return this.numberOfDimensions;
    }


    // MARK: - Convenience Methods

    public final boolean isVoid() {
        return this.getBasicTypeReference().isVoid() && this.numberOfDimensions == 0;
    }
}
