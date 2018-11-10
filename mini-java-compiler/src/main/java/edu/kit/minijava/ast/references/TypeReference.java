package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class TypeReference extends SimpleReference<BasicTypeDeclaration> {
    public TypeReference(String name, int numberOfDimensions, TokenLocation location) {
        super(name, location);

        this.numberOfDimensions = numberOfDimensions;
    }

    private final int numberOfDimensions;

    public final int getNumberOfDimensions() {
        return this.numberOfDimensions;
    }
}
