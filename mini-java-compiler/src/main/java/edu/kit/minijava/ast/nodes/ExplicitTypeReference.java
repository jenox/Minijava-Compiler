package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

public final class ExplicitTypeReference implements TypeReference {
    public ExplicitTypeReference(ExplicitReference<BasicTypeDeclaration> basicTypeReference, int numberOfDimensions) {
        this.basicTypeReference = basicTypeReference;
        this.numberOfDimensions = numberOfDimensions;
    }

    private final ExplicitReference<BasicTypeDeclaration> basicTypeReference;
    private final int numberOfDimensions;

    @Override
    public final ExplicitReference<BasicTypeDeclaration> getBasicTypeReference() {
        return this.basicTypeReference;
    }

    @Override
    public final int getNumberOfDimensions() {
        return this.numberOfDimensions;
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
