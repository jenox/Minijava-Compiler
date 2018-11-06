package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

public final class VariableReference extends AbstractReference<VariableDeclaration> {
    public VariableReference(String name) {
        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    public final String name;
}