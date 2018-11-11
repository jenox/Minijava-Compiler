package edu.kit.minijava.ast.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClassDeclaration implements BasicTypeDeclaration, ASTNode {
    public ClassDeclaration(String name, List<MethodDeclaration> methods, List<FieldDeclaration> fields) {
        this.name = name;
        this.methodDeclarations = Collections.unmodifiableList(methods);
        this.fieldDeclarations = Collections.unmodifiableList(fields);
    }

    private final String name;
    private final List<MethodDeclaration> methodDeclarations;
    private final List<FieldDeclaration> fieldDeclarations;


    private final Map<String, MethodDeclaration> methodSymbolTable = new HashMap<>();
    private final Map<String, FieldDeclaration> fieldSymbolTable = new HashMap<>();

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

    public Map<String, MethodDeclaration> getMethodSymbolTable() {
        return this.methodSymbolTable;
    }

    public Map<String, FieldDeclaration> getFieldSymbolTable() {
        return this.fieldSymbolTable;
    }

    @Override
    public <T, ExceptionType extends Throwable> void accept(ASTVisitor<T, ExceptionType> visitor, T context)
        throws ExceptionType {
        visitor.visit(this, context);
    }
}
