package edu.kit.minijava.ast.nodes;

import java.util.*;

public final class Program implements ASTNode {
    public Program(List<ClassDeclaration> classDeclarations) {
        if (classDeclarations == null) throw new IllegalArgumentException();

        this.classDeclarations = Collections.unmodifiableList(classDeclarations);
    }

    private final List<ClassDeclaration> classDeclarations;

    private Map<String, ClassDeclaration> classSymbolTable = new HashMap<>();

    public List<ClassDeclaration> getClassDeclarations() {
        return this.classDeclarations;
    }

    public Map<String, ClassDeclaration> getClassSymbolTable() {
        return this.classSymbolTable;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }
}
