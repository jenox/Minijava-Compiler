package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

public final class FieldDeclaration extends ASTNode implements VariableDeclaration, MemberDeclaration {
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
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }
}
