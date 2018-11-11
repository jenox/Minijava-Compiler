package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.VariableDeclaration;
import edu.kit.minijava.lexer.TokenLocation;

public final class VariableReference extends SimpleReference<VariableDeclaration> {
    public VariableReference(String name, TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    private final String name;

    public String getName() {
        return this.name;
    }
}
