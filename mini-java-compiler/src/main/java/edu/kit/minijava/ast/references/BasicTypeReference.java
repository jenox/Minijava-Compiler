package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.BasicTypeDeclaration;
import edu.kit.minijava.lexer.TokenLocation;

public final class BasicTypeReference extends SimpleReference<BasicTypeDeclaration> {
    public BasicTypeReference(String name, TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    private final String name;

    public String getName() {
        return this.name;
    }
}
