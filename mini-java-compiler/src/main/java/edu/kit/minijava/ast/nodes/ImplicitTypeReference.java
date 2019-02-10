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
        visitor.willVisit(this);
        visitor.visit(this, context);
        visitor.didVisit(this);
    }

    @Override
    public void substituteExpression(Expression oldValue, Expression newValue) {}

    @Override
    public String toString() {
        return this.basicTypeReference.toString();
    }

    @Override
    public String toStringForDumpingAST() {
        return this.basicTypeReference.toStringForDumpingAST();
    }
}
