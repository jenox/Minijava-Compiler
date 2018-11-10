package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

abstract class SimpleReference<DeclarationType extends Declaration> extends Reference {
    SimpleReference(String name, TokenLocation location) {
        super(name, location);
    }

    private DeclarationType declaration = null;

    @Override
    public final boolean isResolved() {
        return this.declaration != null;
    }

    @Override
    public final DeclarationType getDeclaration() {
        if (!this.isResolved()) throw new IllegalStateException();

        return this.declaration;
    }

    public final void resolveTo(DeclarationType declaration) {
        if (this.isResolved()) throw new IllegalStateException();
        if (declaration == null) throw new IllegalArgumentException();

        this.declaration = declaration;
    }
}
