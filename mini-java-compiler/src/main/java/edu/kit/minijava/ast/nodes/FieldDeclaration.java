package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

public final class FieldDeclaration implements VariableDeclaration, MemberDeclaration, ASTNode {
    public FieldDeclaration(TypeReference type, boolean isFinal, String name, TokenLocation location) {
        this.type = type;
        this.isFinal = isFinal;
        this.name = name;
        this.location = location;
    }

    private final TypeReference type;
    private final boolean isFinal;
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

    @Override
    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public boolean canBeShadowedByVariableDeclarationInNestedScope() {
        return true;
    }

    @Override
    public boolean canBeAccessed() {
        return true;
    }

    @Override
    public boolean isFinal() {
        return this.isFinal;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.willVisit(this);
        visitor.visit(this, context);
        visitor.didVisit(this);
    }

    @Override
    public void substituteExpression(Expression oldValue, Expression newValue) {}

    @Override
    public String toString() {
        return "field '" + this.name + "' at " + this.location;
    }

    @Override
    public String toStringForDumpingAST() {
        return "Field " + this.name + "\n" + this.location;
    }
}
