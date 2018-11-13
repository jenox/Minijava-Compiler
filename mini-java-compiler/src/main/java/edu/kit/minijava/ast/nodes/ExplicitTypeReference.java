package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

public final class ExplicitTypeReference implements TypeReference {
    public ExplicitTypeReference(BasicTypeReference basicTypeReference, int numberOfDimensions) {
        this.basicTypeReference = basicTypeReference;
        this.numberOfDimensions = numberOfDimensions;
    }

    private final BasicTypeReference basicTypeReference;
    private final int numberOfDimensions;

    @Override
    public final BasicTypeReference getBasicTypeReference() {
        return this.basicTypeReference;
    }

    @Override
    public final int getNumberOfDimensions() {
        return this.numberOfDimensions;
    }

    @Override
    public final boolean isVoid() {
        return this.getBasicTypeReference().isVoid() && this.numberOfDimensions == 0;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return this.basicTypeReference.toString();
    }
}
