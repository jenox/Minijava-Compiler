package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class FieldReference extends SimpleReference<FieldDeclaration> {
    public FieldReference(TypeOfExpression context, String name, TokenLocation location) {
        super(name, location);

        if (context == null) throw new IllegalArgumentException();

        this.context = context;
    }

    private final TypeOfExpression context;

    public final TypeOfExpression getContext() {
        return this.context;
    }
}
