package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

import java.util.*;

public final class ClassDeclaration implements BasicTypeDeclaration, ASTNode {
    public ClassDeclaration(String name, List<MethodDeclaration> methods, List<FieldDeclaration> fields,
                            TokenLocation location) {
        this.name = name;
        this.methodDeclarations = Collections.unmodifiableList(methods);
        this.fieldDeclarations = Collections.unmodifiableList(fields);
        this.location = location;
    }

    private final String name;
    private final List<MethodDeclaration> methodDeclarations;
    private final List<FieldDeclaration> fieldDeclarations;
    private final TokenLocation location;

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

    public TokenLocation getLocation() {
        return this.location;
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "class '" + this.name + "' at " + this.location;
    }
}
