package edu.kit.minijava.ast2;

import java.util.*;

public final class ClassDeclaration implements BasicTypeDeclaration {
    public ClassDeclaration(String name, List<MethodDeclaration> staticMethodDeclarations,
                            List<MethodDeclaration> instanceMethodDeclarations,
                            List<FieldDeclaration> fieldDeclarations) {
        this.name = name;
        this.staticMethodDeclarations = Collections.unmodifiableList(staticMethodDeclarations);
        this.instanceMethodDeclarations = Collections.unmodifiableList(instanceMethodDeclarations);
        this.fieldDeclarations = Collections.unmodifiableList(fieldDeclarations);
    }

    public final String name;
    private final List<MethodDeclaration> staticMethodDeclarations;
    private final List<MethodDeclaration> instanceMethodDeclarations;
    private final List<FieldDeclaration> fieldDeclarations;

    @Override
    public List<? extends SubroutineDeclaration> getStaticMethodDeclarations() {
        return this.staticMethodDeclarations;
    }

    @Override
    public List<? extends SubroutineDeclaration> getInstanceMethodDeclarations() {
        return this.instanceMethodDeclarations;
    }

    @Override
    public List<? extends VariableDeclaration> getFieldDeclarations() {
        return this.fieldDeclarations;
    }
}
