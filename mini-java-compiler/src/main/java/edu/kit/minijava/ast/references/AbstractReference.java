package edu.kit.minijava.ast.references;

import edu.kit.minijava.lexer.*;

abstract class AbstractReference<DeclarationType> extends Reference {
    AbstractReference(TokenLocation location) {
        super(location);
    }

    private DeclarationType declaration = null;

    public boolean isResolved() {
        return this.declaration != null;
    }

    public void resolveTo(DeclarationType declaration) {
        if (this.isResolved()) throw new IllegalStateException();
        if (declaration == null) throw new IllegalArgumentException();

        this.declaration = declaration;
    }

    public DeclarationType getDeclaration() {
        if (!this.isResolved()) throw new IllegalStateException();

        return this.declaration;
    }
}
