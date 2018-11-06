package edu.kit.minijava.ast2.nodes;

import java.util.*;

public final class ClassDeclaration extends ASTNode implements BasicTypeDeclaration {
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

    public String getName() {
        return this.name;
    }

    @Override
    public List<MethodDeclaration> getStaticMethodDeclarations() {
        return this.staticMethodDeclarations;
    }

    @Override
    public List<MethodDeclaration> getInstanceMethodDeclarations() {
        return this.instanceMethodDeclarations;
    }

    @Override
    public List<FieldDeclaration> getFieldDeclarations() {
        return this.fieldDeclarations;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
