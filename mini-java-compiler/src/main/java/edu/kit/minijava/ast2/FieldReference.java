package edu.kit.minijava.ast2;

public abstract class FieldReference {
    private FieldReference() {
    }

    public static final class UnresolvedFieldReference extends FieldReference {
        public UnresolvedFieldReference(Expression context, String name) {
            this.context = context;
            this.name = name;
        }

        public final Expression context;
        public final String name;
    }

    public static final class ResolvedFieldReference extends FieldReference {
        public ResolvedFieldReference(FieldDeclaration declaration) {
            this.declaration = declaration;
        }

        public final FieldDeclaration declaration;
    }
}
