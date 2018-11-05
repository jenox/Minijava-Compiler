package edu.kit.minijava.ast2;

public final class TypeReference {

    // no information, computed type
    TypeReference() {
        this.name = null;
        this.basicTypeDeclaration = null;
        this.numberOfDimensions = -1;
    }

    // named type
    TypeReference(String name) {
        this.name = name;
        this.basicTypeDeclaration = null;
        this.numberOfDimensions = -1;
    }

    private String name; // nullable
    private BasicTypeDeclaration basicTypeDeclaration;
    private int numberOfDimensions;

    public boolean isResolved() {
        return this.basicTypeDeclaration != null;
    }

    public void resolveTo(BasicTypeDeclaration declaration, int numberOfDimensions) {
        if (this.isResolved()) throw new IllegalStateException();

        this.basicTypeDeclaration = declaration;
        this.numberOfDimensions = numberOfDimensions;
    }

    public String getName() {
        return this.name;
    }

    public BasicTypeDeclaration getBasicTypeDeclaration() {
        if (!this.isResolved()) throw new IllegalStateException();

        return this.basicTypeDeclaration;
    }

    public int getNumberOfDimensions() {
        if (!this.isResolved()) throw new IllegalStateException();

        return this.numberOfDimensions;
    }
}
