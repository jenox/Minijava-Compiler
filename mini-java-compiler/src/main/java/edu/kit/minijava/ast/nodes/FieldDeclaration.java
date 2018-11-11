package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.TypeReference;
import edu.kit.minijava.lexer.TokenLocation;

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
    public boolean canDeclarationBeShadowed() {
        return true;
    }

    public String getName() {
        return this.name;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public <T, ExceptionType extends Throwable> void accept(ASTVisitor<T, ExceptionType> visitor, T context)
        throws ExceptionType {
        visitor.visit(this, context);
    }
}
