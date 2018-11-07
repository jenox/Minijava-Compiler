package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class TypeReference extends SimpleReference<BasicTypeDeclaration> {
    public TypeReference(String name, int numberOfDimensions, TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();

        this.name = name;
        this.numberOfDimensions = numberOfDimensions;
    }

    public TypeReference(String name, PrimitiveTypeDeclaration declaration, int numberOfDimensions,
                         TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();
        if (declaration == null) throw new IllegalArgumentException();

        this.name = name;
        this.numberOfDimensions = numberOfDimensions;

        this.resolveTo(declaration);
    }

    public TypeReference(BasicTypeReference reference, int numberOfDimensions) {
        super(reference.getLocation());

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
