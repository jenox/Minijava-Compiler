package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

import java.util.*;
import java.util.stream.*;

public final class MethodDeclaration extends ASTNode implements SubroutineDeclaration, MemberDeclaration {
    public MethodDeclaration(boolean isStatic, TypeReference returnType, String name,
                             List<ParameterDeclaration> parameters, Statement.Block body) {
        this.isStatic = isStatic;
        this.returnType = returnType;
        this.name = name;
        this.parameters = Collections.unmodifiableList(parameters);
        this.body = body;
    }

    private final boolean isStatic;
    private final TypeReference returnType;
    private final String name;
    private final List<ParameterDeclaration> parameters;
    private final Statement.Block body;

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

    @Override
    public List<TypeReference> getParameterTypes() {
        return this.parameters.stream().map(ParameterDeclaration::getType).collect(Collectors.toList());
    }

    public Statement.Block getBody() {
        return this.body;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }
}
