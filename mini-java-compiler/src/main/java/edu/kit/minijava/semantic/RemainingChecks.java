package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;

public class RemainingChecks extends ASTVisitor<Void> {
    public RemainingChecks(Program program) {
        this.symbolTable = new SymbolTable();

        program.accept(this);
    }

    private final SymbolTable symbolTable;


    // MARK: - Traversal

    @Override
    protected void visit(Program program, Void context) {
        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        assert this.symbolTable.getNumberOfScopes() == 0;

        this.symbolTable.enterNewScope();

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this);
        }

        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {
            methodDeclaration .accept(this);
        }

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        this.symbolTable.enterDeclaration(fieldDeclaration);
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        this.symbolTable.enterNewScope();

        for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
            parameterDeclaration.accept(this);
        }

        methodDeclaration.getBody().accept(this);

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        this.symbolTable.enterDeclaration(parameterDeclaration);
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
    protected void visit(Statement.ExpressionStatement statement, Void context) {}

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {}

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {}

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {
        assert !this.symbolTable.hasVariableDeclarationInCurrentScopeWithName(statement.getName()) :
        "variable already defined in current scope";

        Optional<VariableDeclaration> declaration = this.symbolTable.getVisibleDeclarationForName(statement.getName());

        if (declaration.isPresent()) {
            assert declaration.get().canBeShadowedByVariableDeclarationInNestedScope() :
            declaration.get() + " cannot be shadowed by " + statement;
        }

        this.symbolTable.enterDeclaration(statement);
    }

    @Override
    protected void visit(Statement.Block block, Void context) {
        this.symbolTable.enterNewScope();

        for (Statement statement : block.getStatements()) {
            statement.accept(this);
        }

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(Expression.BinaryOperation expression, Void context) {}

    @Override
    protected void visit(Expression.UnaryOperation expression, Void context) {}

    @Override
    protected void visit(Expression.NullLiteral expression, Void context) {}

    @Override
    protected void visit(Expression.BooleanLiteral expression, Void context) {}

    @Override
    protected void visit(Expression.IntegerLiteral expression, Void context) {}

    @Override
    protected void visit(Expression.MethodInvocation expression, Void context) {}

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Void context) {}

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {}

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {}

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {}

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {}

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {}
}
