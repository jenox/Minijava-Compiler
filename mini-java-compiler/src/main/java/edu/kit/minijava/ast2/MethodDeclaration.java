package edu.kit.minijava.ast2;

import java.util.*;
import java.util.stream.*;

public final class MethodDeclaration implements SubroutineDeclaration {
    public MethodDeclaration(TypeReference returnType, String name, List<ParameterDeclaration> parameterDeclarations) {
        this.returnType = returnType;
        this.name = name;
        this.parameterDeclarations = Collections.unmodifiableList(parameterDeclarations);
    }

    private final TypeReference returnType;
    private final String name;
    private final List<ParameterDeclaration> parameterDeclarations;

    @Override
    public List<? extends TypeReference> getParameterTypes() {
        return this.parameterDeclarations.stream().map(ParameterDeclaration::getType).collect(Collectors.toList());
    }

    @Override
    public TypeReference getReturnType() {
        return this.returnType;
    }
}
