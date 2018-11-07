package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

import java.util.*;

public final class MethodReference extends AbstractReference<MethodDeclaration> {
    public MethodReference(TypeOfExpression context, String name, List<TypeOfExpression> argumentTypes,
                           TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();
        if (argumentTypes == null) throw new IllegalArgumentException();

        this.context = context;
        this.name = name;
        this.argumentTypes = Collections.unmodifiableList(argumentTypes);
    }

    private final TypeOfExpression context; // nullable
    private final String name;
    private final List<TypeOfExpression> argumentTypes;

    public TypeOfExpression getContext() {
        return this.context;
    }

    public String getName() {
        return this.name;
    }

    public List<TypeOfExpression> getArgumentTypes() {
        return this.argumentTypes;
    }
}
