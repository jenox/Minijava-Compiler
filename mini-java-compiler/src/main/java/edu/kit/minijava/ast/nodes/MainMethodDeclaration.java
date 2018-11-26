package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

import java.util.*;

public final class MainMethodDeclaration implements SubroutineDeclaration, MemberDeclaration, ASTNode {
    public MainMethodDeclaration(String name, Token argumentsParameterNameToken, Statement.Block body,
                                 TokenLocation location) {
        ImplicitTypeReference parameterType = new ImplicitTypeReference(PrimitiveTypeDeclaration.STRING, 1);
        String parameterName = argumentsParameterNameToken.getText();
        TokenLocation parameterLocation = argumentsParameterNameToken.getLocation();
        ParameterDeclaration parameter = new ParameterDeclaration(parameterType, parameterName, parameterLocation,
                false);

        this.returnType = new ImplicitTypeReference(PrimitiveTypeDeclaration.VOID, 0);
        this.name = name;
        this.argumentsParameter = parameter;
        this.body = body;
        this.location = location;
    }

    private final TypeReference returnType;
    private final String name;
    private final ParameterDeclaration argumentsParameter;
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

    public VariableDeclaration getArgumentsParameter() {
        return this.argumentsParameter;
    }

    public Statement.Block getBody() {
        return this.body;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public List<TypeReference> getParameterTypes() {
        return Collections.singletonList(this.argumentsParameter.getType());
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
        return "main method '" + this.name + "' at " + this.location;
    }
}
