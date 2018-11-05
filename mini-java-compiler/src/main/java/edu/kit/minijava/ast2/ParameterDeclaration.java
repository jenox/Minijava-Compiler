package edu.kit.minijava.ast2;

public final class ParameterDeclaration implements VariableDeclaration {
    public ParameterDeclaration(TypeReference type, String name) {
        this.type = type;
        this.name = name;
    }

    private final TypeReference type;
    private final String name;

    @Override
    public TypeReference getType() {
        return this.type;
    }
}
