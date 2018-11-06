package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

import java.util.*;
import java.util.stream.*;

public final class MethodDeclaration extends ASTNode implements MemberDeclaration, SubroutineDeclaration {
    public MethodDeclaration(TypeReference returnType, String name, List<ParameterDeclaration> parameters,
                             Statement.Block body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = Collections.unmodifiableList(parameters);
        this.body = body;
    }

    private final TypeReference returnType;
    private final String name;
    private final List<ParameterDeclaration> parameters;
    private final Statement.Block body;

    @Override
    public List<? extends TypeReference> getParameterTypes() {
        return this.parameters.stream().map(ParameterDeclaration::getType).collect(Collectors.toList());
    }

    @Override
    public TypeReference getReturnType() {
        return this.returnType;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
