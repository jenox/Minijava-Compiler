package edu.kit.minijava.ast2;

import java.util.*;

public abstract class MethodReference {
    private MethodReference() {
    }

    public static final class UnresolvedMethodReference extends MethodReference {
        public UnresolvedMethodReference(Expression context, String name, List<TypeReference> argumentTypes) {
            this.context = context;
            this.name = name;
            this.argumentTypes = argumentTypes;
        }

        public final Expression context; // nullable
        public final String name;
        public final List<TypeReference> argumentTypes;
    }

    public static final class ResolvedMethodReference extends MethodReference {
        public ResolvedMethodReference(MethodDeclaration declaration) {
            this.declaration = declaration;
        }

        public final MethodDeclaration declaration;
    }
}
