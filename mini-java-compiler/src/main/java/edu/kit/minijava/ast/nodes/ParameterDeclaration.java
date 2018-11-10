package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;
import edu.kit.minijava.lexer.*;

public final class ParameterDeclaration implements VariableDeclaration, ASTNode {
    public ParameterDeclaration(TypeReference type, String name, TokenLocation location) {
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

    public String getName() {
        return this.name;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "parameter '" + this.name + "' at " + this.location;
    }
}
