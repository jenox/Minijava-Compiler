package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.FieldDeclaration;
import edu.kit.minijava.lexer.TokenLocation;

public final class FieldReference extends SimpleReference<FieldDeclaration> {
    public FieldReference(TypeOfExpression context, String name, TokenLocation location) {
        super(location);

        if (context == null) throw new IllegalArgumentException();
        if (name == null) throw new IllegalArgumentException();

        this.context = context;
        this.name = name;
    }

    private final TypeOfExpression context;
    private final String name;

    public TypeOfExpression getContext() {
        return this.context;
    }

    public String getName() {
        return this.name;
    }
}
