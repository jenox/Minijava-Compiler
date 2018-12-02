package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

import java.util.*;

public final class ClassDeclaration implements BasicTypeDeclaration, ASTNode {
    public ClassDeclaration(String name, List<MainMethodDeclaration> mainMethodDeclarations,
                    List<MethodDeclaration> methodDeclarations, List<FieldDeclaration> fieldDeclarations,
                    TokenLocation location) {
        this.name = name;
        this.mainMethodDeclarations = Collections.unmodifiableList(mainMethodDeclarations);
        this.methodDeclarations = Collections.unmodifiableList(methodDeclarations);
        this.fieldDeclarations = Collections.unmodifiableList(fieldDeclarations);
        this.location = location;
    }

    private final String name;
    private final List<MainMethodDeclaration> mainMethodDeclarations;
    private final List<MethodDeclaration> methodDeclarations;
    private final List<FieldDeclaration> fieldDeclarations;
    private final TokenLocation location;

    @Override
    public String getName() {
        return this.name;
    }

    public List<MainMethodDeclaration> getMainMethodDeclarations() {
        return this.mainMethodDeclarations;
    }

    @Override
    public List<MethodDeclaration> getMethodDeclarations() {
        return this.methodDeclarations;
    }

    @Override
    public List<FieldDeclaration> getFieldDeclarations() {
        return this.fieldDeclarations;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.willVisit(this);
        visitor.visit(this, context);
        visitor.didVisit(this);
    }

    @Override
    public void substituteExpression(Expression oldValue, Expression newValue) {}

    @Override
    public boolean isClassDeclaration() {
        return true;
    }

    @Override
    public String toString() {
        return "class '" + this.name + "' at " + this.location;
    }
}
