package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class FieldReference extends AbstractReference<FieldDeclaration> {
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
