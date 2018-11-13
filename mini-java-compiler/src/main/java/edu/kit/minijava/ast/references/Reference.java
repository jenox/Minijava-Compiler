package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

public abstract class Reference<DeclarationType extends Declaration> {

    /** Whether or not the reference has been resolved and the declaration can be accessed. */
    public abstract boolean isResolved();

    /** If resolved, the declaration this reference points to. */
    public abstract DeclarationType getDeclaration();
}
