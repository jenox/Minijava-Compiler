package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

import java.util.*;
import java.util.stream.*;

public final class MethodDeclaration implements SubroutineDeclaration, MemberDeclaration, ASTNode {
    public MethodDeclaration(TypeReference returnType, String name, List<ParameterDeclaration> parameters,
                             Statement.Block body, TokenLocation location) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = Collections.unmodifiableList(parameters);
        this.body = body;
        this.location = location;
    }

    private final TypeReference returnType;
    private final String name;
    private final List<ParameterDeclaration> parameters;
    private final Statement.Block body;
    private final TokenLocation location;

    @Override
    public TypeReference getReturnType() {
        return this.returnType;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public List<? extends VariableDeclaration> getParameters() {
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
        visitor.willVisit(this);
        visitor.visit(this, context);
        visitor.didVisit(this);
    }

    @Override
    public void substituteExpression(Expression oldValue, Expression newValue) {}

    @Override
    public String toString() {
        return "instance method '" + this.name + "' at " + this.location;
    }

    @Override
    public String toStringForDumpingAST() {
        return "Method " + this.name + "\n" + this.location;
    }
}
