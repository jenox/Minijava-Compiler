package edu.kit.minijava.ast.nodes;

import java.util.*;

public final class ClassDeclaration implements BasicTypeDeclaration, ASTNode {
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
    public <T, ExceptionType extends Throwable> void accept(ASTVisitor<T, ExceptionType> visitor, T context) throws ExceptionType {
        visitor.visit(this, context);
    }
}
