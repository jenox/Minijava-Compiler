package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class BasicTypeReference extends SimpleReference<BasicTypeDeclaration> {
    public BasicTypeReference(String name, TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    public BasicTypeReference(String name, PrimitiveTypeDeclaration declaration, TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();
        if (declaration == null) throw new IllegalArgumentException();

        this.name = name;

        this.resolveTo(declaration);
    }

    public BasicTypeReference(PrimitiveTypeDeclaration declaration, TokenLocation location) {
        super(location);

        if (declaration == null) throw new IllegalArgumentException();

        this.name = null;

        this.resolveTo(declaration);
    }

    private final String name;

    public String getName() {
        return this.name;
    }
}
