package edu.kit.minijava.ast2.references;

import edu.kit.minijava.ast2.nodes.*;

public final class TypeReference extends AbstractReference<BasicTypeDeclaration> {
    public TypeReference() {
        this.name = null;
        this.numberOfDimensions = 0;
    }

    public TypeReference(BasicTypeReference reference, int numberOfDimensions) {
        if (reference == null) throw new IllegalArgumentException();

        this.name = reference.name;
        this.numberOfDimensions = numberOfDimensions;

        if (reference.isResolved()) {
            this.resolveTo(reference.getDeclaration());
        }
    }

    public final String name; // nullable
    public final int numberOfDimensions;
}
