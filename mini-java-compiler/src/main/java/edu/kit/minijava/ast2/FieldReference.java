package edu.kit.minijava.ast2;

public final class FieldReference extends AbstractReference<FieldDeclaration> {
    public FieldReference(TypeReference context, String name) {
        if (context == null) throw new IllegalArgumentException();
        if (name == null) throw new IllegalArgumentException();

        this.context = context;
        this.name = name;
    }

    public final TypeReference context;
    public final String name;
}
