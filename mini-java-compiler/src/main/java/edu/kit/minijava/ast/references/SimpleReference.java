package edu.kit.minijava.ast.references;

import edu.kit.minijava.lexer.*;

abstract class SimpleReference<DeclarationType> extends Reference {
    SimpleReference(String name, TokenLocation location) {
        super(name, location);
    }

    private DeclarationType declaration = null;

    public final boolean isResolved() {
        return this.declaration != null;
    }

    public final void resolveTo(DeclarationType declaration) {
        if (this.isResolved()) throw new IllegalStateException();
        if (declaration == null) throw new IllegalArgumentException();

        this.declaration = declaration;
    }

    public final DeclarationType getDeclaration() {
        if (!this.isResolved()) throw new IllegalStateException();

        return this.declaration;
    }
}
