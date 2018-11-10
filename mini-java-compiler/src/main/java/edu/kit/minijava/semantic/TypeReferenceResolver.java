package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

public final class TypeReferenceResolver extends ASTVisitor<Void> {

    public TypeReferenceResolver(Program program, ClassAndMemberNameConflictChecker checker) {
        this.checker = checker;

        program.accept(this);
    }

    private final ClassAndMemberNameConflictChecker checker;

    @Override
    protected void visit(Program program, Void context) {
        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {
            methodDeclaration.accept(this);
        }

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration .accept(this);
        }
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        this.resolve(fieldDeclaration.getType());
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        this.resolve(methodDeclaration.getReturnType());

        for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
            parameterDeclaration.accept(this);
        }

        methodDeclaration.getBody().accept(this);
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        this.resolve(parameterDeclaration.getType());
    }

    @Override
    protected void visit(Statement.IfStatement statement, Void context) {
        statement.getStatementIfTrue().accept(this);
        statement.getStatementIfFalse().ifPresent(s -> s.accept(this));
    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        statement.getStatementWhileTrue().accept(this);
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {}

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {}

    @Override
    protected void visit(Statement.Block block, Void context) {
        for (Statement statement : block.getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {
        this.resolve(statement.getType());
    }

    private void resolve(TypeReference reference) {
        assert !reference.isResolved();

        switch (reference.getName()) {
            case "void":
                reference.resolveTo(PrimitiveTypeDeclaration.VOID);
                break;
            case "int":
                reference.resolveTo(PrimitiveTypeDeclaration.INTEGER);
                break;
            case "boolean":
                reference.resolveTo(PrimitiveTypeDeclaration.BOOLEAN);
                break;
            default:
                assert this.checker.getClassDeclaration(reference.getName()) != null :
                "use of undeclared identifier " + reference.getName() + " at " + reference.getLocation();

                reference.resolveTo(this.checker.getClassDeclaration(reference.getName()));
                break;
        }

        System.out.println("Resolved " + reference + " to " + reference.getDeclaration());
    }
}
