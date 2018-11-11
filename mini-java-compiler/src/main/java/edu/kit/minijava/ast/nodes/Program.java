package edu.kit.minijava.ast.nodes;

import java.util.*;

public final class Program implements ASTNode {
    public Program(List<ClassDeclaration> classDeclarations) {
        if (classDeclarations == null) throw new IllegalArgumentException();

        this.classDeclarations = Collections.unmodifiableList(classDeclarations);
    }

    private final List<ClassDeclaration> classDeclarations;

    public List<ClassDeclaration> getClassDeclarations() {
        return this.classDeclarations;
    }

    @Override
    public <T, ExceptionType extends Throwable> void accept(ASTVisitor<T, ExceptionType> visitor, T context) throws ExceptionType {
        visitor.visit(this, context);
    }
}
