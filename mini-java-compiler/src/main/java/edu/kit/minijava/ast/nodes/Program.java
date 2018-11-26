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
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.willVisit(this);
        visitor.visit(this, context);
        visitor.didVisit(this);
    }

    @Override
    public void substituteExpression(Expression oldValue, Expression newValue) {}
}
