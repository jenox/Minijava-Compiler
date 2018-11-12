package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;
import edu.kit.minijava.lexer.*;

import java.util.*;
import java.util.stream.*;

public final class MainMethodDeclaration implements SubroutineDeclaration, MemberDeclaration, ASTNode {
    public MainMethodDeclaration(TypeReference returnType, String name, ParameterDeclaration parameter,
                                 Statement.Block body, TokenLocation location) {
        assert returnType.getName().equals("void") && returnType.getNumberOfDimensions() == 0;
        assert parameter.getType().getName().equals("String") && parameter.getType().getNumberOfDimensions() == 1;

        this.returnType = returnType;
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

    public ParameterDeclaration getArgumentsParameter() {
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
        visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "main method '" + this.name + "' at " + this.location;
    }
}
