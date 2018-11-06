package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public final class MethodReference extends AbstractReference<MethodDeclaration> {
    public MethodReference(TypeReference context, String name, List<TypeReference> argumentTypes) {
        if (name == null) throw new IllegalArgumentException();
        if (argumentTypes == null) throw new IllegalArgumentException();

        this.context = context;
        this.name = name;
        this.argumentTypes = Collections.unmodifiableList(argumentTypes);
    }

    private final TypeReference context; // nullable
    private final String name;
    private final List<TypeReference> argumentTypes;

    public TypeReference getContext() {
        return this.context;
    }

    public String getName() {
        return this.name;
    }

    public List<TypeReference> getArgumentTypes() {
        return this.argumentTypes;
    }
}
