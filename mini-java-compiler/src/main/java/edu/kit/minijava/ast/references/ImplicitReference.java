package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

/**
 * An implicit reference to a declaration, i.e. one that does not appear in the source code.
 */
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

    @Override
    public String toString() {
        return "#implicit";
    }

    @Override
    public String toStringForDumpingAST() {
        return "Implicit Reference";
    }
}
