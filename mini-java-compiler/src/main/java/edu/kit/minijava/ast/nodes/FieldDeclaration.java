package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

public final class FieldDeclaration extends ASTNode implements VariableDeclaration, MemberDeclaration {
    public FieldDeclaration(TypeReference type, String name) {
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
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }
}
