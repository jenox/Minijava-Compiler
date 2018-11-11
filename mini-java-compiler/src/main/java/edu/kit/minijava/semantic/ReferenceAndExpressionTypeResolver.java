package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;

public class ReferenceAndExpressionTypeResolver extends ASTVisitor<ClassDeclaration> {
    public ReferenceAndExpressionTypeResolver(Program program) {
        this.symbolTable = new SymbolTable();

        program.accept(this);
    }

    private final SymbolTable symbolTable;


    // MARK: - Traversal

    @Override
    protected void visit(Program program, ClassDeclaration context) {
        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this, context);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, ClassDeclaration context) {
        assert this.symbolTable.getNumberOfScopes() == 0;

        this.symbolTable.enterNewScope();

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this, classDeclaration);
        }

        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {
            methodDeclaration .accept(this, classDeclaration);
        }

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, ClassDeclaration context) {
        this.symbolTable.enterDeclaration(fieldDeclaration);
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, ClassDeclaration context) {
        this.symbolTable.enterNewScope();

        for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
            parameterDeclaration.accept(this, context);
        }

        methodDeclaration.getBody().accept(this, context);

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, ClassDeclaration context) {
        this.symbolTable.enterDeclaration(parameterDeclaration);
    }

    @Override
    protected void visit(Statement.IfStatement statement, ClassDeclaration context) {
        statement.getCondition().accept(this, context);
        statement.getStatementIfTrue().accept(this, context);
        statement.getStatementIfFalse().ifPresent(node -> node.accept(this, context));
    }

    @Override
    protected void visit(Statement.WhileStatement statement, ClassDeclaration context) {
        statement.getCondition().accept(this, context);
        statement.getStatementWhileTrue().accept(this, context);
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, ClassDeclaration context) {
        statement.getExpression().accept(this, context);
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, ClassDeclaration context) {
        statement.getValue().ifPresent(node -> node.accept(this, context));
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, ClassDeclaration context) {}

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, ClassDeclaration context) {
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
    protected void visit(Statement.Block block, ClassDeclaration context) {
        this.symbolTable.enterNewScope();

        for (Statement statement : block.getStatements()) {
            statement.accept(this, context);
        }

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(Expression.BinaryOperation expression, ClassDeclaration context) {
        expression.getLeft().accept(this, context);
        expression.getRight().accept(this, context);
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, ClassDeclaration context) {
        expression.getOther().accept(this, context);
    }

    @Override
    protected void visit(Expression.NullLiteral expression, ClassDeclaration context) {
        expression.getType().resolveToNull();
    }

    @Override
    protected void visit(Expression.BooleanLiteral expression, ClassDeclaration context) {
        expression.getType().resolveTo(PrimitiveTypeDeclaration.BOOLEAN);
    }

    @Override
    protected void visit(Expression.IntegerLiteral expression, ClassDeclaration context) {
        expression.getType().resolveTo(PrimitiveTypeDeclaration.INTEGER);
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, ClassDeclaration context) {
        expression.getContext().ifPresent(node -> node.accept(this, context));

        System.out.println("method access " + expression.getReference());
        System.out.println();

        expression.getArguments().forEach(node -> node.accept(this, context));
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, ClassDeclaration context) {
        expression.getContext().accept(this, context);
//        expression.getReference()

        System.out.println("field access " + expression.getReference());
        System.out.println(expression.getContext());
        System.out.println(expression.getContext().getType());
        System.out.println(expression.getType());
        System.out.println();
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, ClassDeclaration context) {
        expression.getContext().accept(this, context);
        expression.getIndex().accept(this, context);
    }

    @Override
    protected void visit(Expression.VariableAccess expression, ClassDeclaration context) {
        String name = expression.getReference().getName();
        Optional<VariableDeclaration> declaration = this.symbolTable.getVisibleDeclarationForName(name);

        assert declaration.isPresent() : "use of undeclared variable " + name;

        expression.getReference().resolveTo(declaration.get());
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, ClassDeclaration context) {
        expression.getType().resolveTo(context);
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, ClassDeclaration context) {
//        expression.getType().resolveTo(expression.getReference().getDeclaration());
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, ClassDeclaration context) {
        expression.getPrimaryDimension().accept(this, context);
    }
}
