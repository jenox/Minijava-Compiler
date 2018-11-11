package edu.kit.minijava.ast.references;

import edu.kit.minijava.lexer.*;

abstract class SimpleReference<DeclarationType> extends Reference {
    SimpleReference(TokenLocation location) {
        super(location);
    }

    private DeclarationType declaration = null;

    public boolean isResolved() {
        return this.declaration != null;
    }

    public void resolveTo(DeclarationType declaration) {
        if (declaration == null) throw new IllegalArgumentException();

        this.declaration = declaration;
    }

    public DeclarationType getDeclaration() {
        if (!this.isResolved()) throw new IllegalStateException();

        return this.declaration;
    }
}
