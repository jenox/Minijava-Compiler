package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.BasicTypeDeclaration;
import edu.kit.minijava.lexer.TokenLocation;

public final class TypeReference extends SimpleReference<BasicTypeDeclaration> {
    public TypeReference(String name, int numberOfDimensions, TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();

        this.name = name;
        this.numberOfDimensions = numberOfDimensions;
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
