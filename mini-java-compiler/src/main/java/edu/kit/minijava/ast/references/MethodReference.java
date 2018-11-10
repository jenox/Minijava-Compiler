package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

import java.util.*;

public final class MethodReference extends SimpleReference<MethodDeclaration> {
    public MethodReference(TypeOfExpression context, String name, List<TypeOfExpression> argumentTypes,
                           TokenLocation location) {
        super(name, location);

        if (argumentTypes == null) throw new IllegalArgumentException();

        this.context = context;
        this.argumentTypes = Collections.unmodifiableList(argumentTypes);
    }

    private final TypeOfExpression context; // nullable
    private final List<TypeOfExpression> argumentTypes;

    public final TypeOfExpression getContext() {
        return this.context;
    }

    public final List<TypeOfExpression> getArgumentTypes() {
        return this.argumentTypes;
    }
}
