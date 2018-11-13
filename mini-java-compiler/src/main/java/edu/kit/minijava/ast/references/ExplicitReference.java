package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

/**
 * An explicit reference to a declaration in code, i.e. one that has a name and a location in the source code.
 */
public class ExplicitReference<DeclarationType extends Declaration> extends Reference<DeclarationType> {
    public ExplicitReference(String name, TokenLocation location) {
        this.name = name;
        this.location = location;
    }

    private final String name;
    private final TokenLocation location;
    private DeclarationType declaration = null;

    public final String getName() {
        return this.name;
    }

    public final TokenLocation getLocation() {
        return this.location;
    }

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

    @Override
    public String toString() {
        return this.name + " at " + this.location;
    }
}
