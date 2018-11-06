package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

public final class FieldDeclaration extends ASTNode implements MemberDeclaration, VariableDeclaration {
    public FieldDeclaration(TypeReference type, String name) {
        this.type = type;
        this.name = name;
    }

    private final TypeReference type;
    private final String name;

    public String getName() {
        return this.name;
    }

    @Override
    public TypeReference getType() {
        return this.type;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
