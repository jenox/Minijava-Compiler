package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;
import edu.kit.minijava.lexer.*;

import java.util.*;
import java.util.stream.*;

public final class MethodDeclaration implements SubroutineDeclaration, MemberDeclaration, ASTNode {
    public MethodDeclaration(boolean isStatic, TypeReference returnType, String name,
                             List<ParameterDeclaration> parameters, Statement.Block body, TokenLocation location) {
        this.isStatic = isStatic;
        this.returnType = returnType;
        this.name = name;
        this.parameters = Collections.unmodifiableList(parameters);
        this.body = body;
        this.location = location;
    }

    private final boolean isStatic;
    private final TypeReference returnType;
    private final String name;
    private final List<ParameterDeclaration> parameters;
    private final Statement.Block body;
    private final TokenLocation location;

    public boolean isStatic() {
        return this.isStatic;
    }

    @Override
    public TypeReference getReturnType() {
        return this.returnType;
    }

    public String getName() {
        return this.name;
    }

    public List<ParameterDeclaration> getParameters() {
        return this.parameters;
    }

    public Statement.Block getBody() {
        return this.body;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public List<TypeReference> getParameterTypes() {
        return this.parameters.stream().map(ParameterDeclaration::getType).collect(Collectors.toList());
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }

    @Override
    public String toString() {
        if (this.isStatic) {
            return "static method '" + this.name + "' at " + this.location;
        }
        else {
            return "instance method '" + this.name + "' at " + this.location;
        }
    }
}
