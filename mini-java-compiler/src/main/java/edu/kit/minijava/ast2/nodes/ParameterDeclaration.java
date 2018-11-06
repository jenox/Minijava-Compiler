package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

public final class ParameterDeclaration extends ASTNode implements VariableDeclaration {
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

    public String getName() {
        return this.name;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
