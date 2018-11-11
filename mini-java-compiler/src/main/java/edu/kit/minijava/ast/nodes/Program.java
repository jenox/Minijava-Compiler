package edu.kit.minijava.ast.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public <T, ExceptionType extends Throwable> void accept(ASTVisitor<T, ExceptionType> visitor, T context)
        throws ExceptionType {
        visitor.visit(this, context);
    }
}
