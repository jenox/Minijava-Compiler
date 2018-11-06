package edu.kit.minijava.ast.nodes;

import java.util.*;

public final class Program extends ASTNode {
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
        visitor.visit(this, context);
    }
}
