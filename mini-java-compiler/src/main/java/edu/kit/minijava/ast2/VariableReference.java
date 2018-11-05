package edu.kit.minijava.ast2;

public abstract class VariableReference {
    private VariableReference() {
    }

    public static final class UnresolvedVariableReference extends VariableReference {
        public UnresolvedVariableReference(String name) {
            this.name = name;
        }

        public final String name;
    }

    public static final class ResolvedVariableReference extends VariableReference {
        public ResolvedVariableReference(VariableDeclaration declaration) {
            this.declaration = declaration;
        }

        public final VariableDeclaration declaration;
    }
}
