package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

import java.util.*;

public final class MethodReference extends ExplicitReference<MethodDeclaration> {
    public MethodReference(String name, List<TypeOfExpression> argumentTypes,
                           TokenLocation location) {
        super(name, location);

        if (argumentTypes == null) throw new IllegalArgumentException();

        this.context = null;
        this.argumentTypes = Collections.unmodifiableList(argumentTypes);
    }

    public MethodReference(TypeOfExpression context, String name, List<TypeOfExpression> argumentTypes,
                           TokenLocation location) {
        super(name, location);

        if (argumentTypes == null) throw new IllegalArgumentException();

        this.context = context;
        this.argumentTypes = Collections.unmodifiableList(argumentTypes);
    }

    private final TypeOfExpression context; // nullable
    private final List<TypeOfExpression> argumentTypes;

    public final Optional<TypeOfExpression> getContext() {
        return Optional.ofNullable(this.context);
    }

    public final List<TypeOfExpression> getArgumentTypes() {
        return this.argumentTypes;
    }
}
