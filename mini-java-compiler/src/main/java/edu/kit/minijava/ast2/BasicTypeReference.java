package edu.kit.minijava.ast2;

public abstract class BasicTypeReference {
    private BasicTypeReference() {
    }

    public static final class UnresolvedBasicTypeReference extends BasicTypeReference {
        public UnresolvedBasicTypeReference(String name) {
            this.name = name;
        }

        public final String name;
    }

    public static final class ResolvedBasicTypeReference extends BasicTypeReference {
        public ResolvedBasicTypeReference(BasicTypeDeclaration declaration) {
            this.declaration = declaration;
        }

        public final BasicTypeDeclaration declaration;
    }
}
