package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

public class ImplicitTypeReference implements TypeReference {
    public ImplicitTypeReference(BasicTypeDeclaration declaration, int numberOfDimensions) {
        this.basicTypeReference = new ImplicitReference<>(declaration);
        this.numberOfDimensions = numberOfDimensions;
    }

    private final ImplicitReference<BasicTypeDeclaration> basicTypeReference;
    private final int numberOfDimensions;

    @Override
    public final ImplicitReference<BasicTypeDeclaration> getBasicTypeReference() {
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
