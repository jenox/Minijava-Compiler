package edu.kit.minijava.ast2.references;

import edu.kit.minijava.ast2.nodes.*;

import java.util.*;

public final class MethodReference extends AbstractReference<MethodDeclaration> {
    public MethodReference(TypeReference context, String name, List<TypeReference> argumentTypes) {
        if (name == null) throw new IllegalArgumentException();
        if (argumentTypes == null) throw new IllegalArgumentException();

        this.context = context;
        this.name = name;
        this.argumentTypes = Collections.unmodifiableList(argumentTypes);
    }

    public final TypeReference context; // nullable
    public final String name;
    public final List<TypeReference> argumentTypes;
}
