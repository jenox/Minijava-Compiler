package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

public abstract class Reference<DeclarationType extends Declaration> {
    public abstract boolean isResolved();
    public abstract DeclarationType getDeclaration();
}
