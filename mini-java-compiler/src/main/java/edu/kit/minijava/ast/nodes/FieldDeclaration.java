package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

public final class FieldDeclaration implements VariableDeclaration, MemberDeclaration, ASTNode {
    public FieldDeclaration(TypeReference type, String name, TokenLocation location) {
        this.type = type;
        this.name = name;
        this.location = location;
    }

    private final TypeReference type;
    private final String name;
    private final TokenLocation location;

    @Override
    public TypeReference getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public boolean canBeShadowedByVariableDeclarationInNestedScope() {
        return true;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "field '" + this.name + "' at " + this.location;
    }
}
