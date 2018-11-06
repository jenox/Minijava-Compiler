package edu.kit.minijava.ast2.nodes;

import java.util.*;

public final class ClassDeclaration extends ASTNode implements BasicTypeDeclaration {
    public ClassDeclaration(String name, List<MethodDeclaration> methods, List<FieldDeclaration> fields) {
        this.name = name;
        this.methodDeclarations = Collections.unmodifiableList(methods);
        this.fieldDeclarations = Collections.unmodifiableList(fields);
    }

    private final String name;
    private final List<MethodDeclaration> methodDeclarations;
    private final List<FieldDeclaration> fieldDeclarations;

    public String getName() {
        return this.name;
    }

    @Override
    public List<MethodDeclaration> getMethodDeclarations() {
        return this.methodDeclarations;
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
