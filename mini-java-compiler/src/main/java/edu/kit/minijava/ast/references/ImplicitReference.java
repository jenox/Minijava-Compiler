package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

public class ImplicitReference<DeclarationType extends Declaration> extends Reference<DeclarationType> {
    public ImplicitReference(DeclarationType declaration) {
        this.declaration = declaration;
    }

    private final DeclarationType declaration;

    @Override
    public final boolean isResolved() {
        return true;
    }

    @Override
    public final DeclarationType getDeclaration() {
        return this.declaration;
    }
}
