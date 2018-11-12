package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class Collector extends ASTVisitor<Void> {

    public Collector(Program program) {
        program.accept(this);
    }

    private final List<Object> elements = new ArrayList<>();

    public <T> List<T> instancesOfClass(Class<T> genericClass) {
        List<T> instances = new ArrayList<>();

        for (Object element : this.elements) {
            try {
                instances.add(genericClass.cast(element));
            }
            catch (ClassCastException exception) {}
        }

        return instances;
    }

    @Override
    protected void visit(Program program, Void context) {
        this.elements.add(program);

        program.getClassDeclarations().forEach(d -> d.accept(this));
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        this.elements.add(classDeclaration);

        classDeclaration.getMethodDeclarations().forEach(d -> d.accept(this));
        classDeclaration.getFieldDeclarations().forEach(d -> d.accept(this));
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        this.elements.add(fieldDeclaration);

        this.elements.add(fieldDeclaration.getType());
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        this.elements.add(methodDeclaration);

        this.elements.add(methodDeclaration.getReturnType());
        methodDeclaration.getParameters().forEach(d -> d.accept(this));
        methodDeclaration.getBody().accept(this);
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        this.elements.add(methodDeclaration);

        this.elements.add(methodDeclaration.getReturnType());
        methodDeclaration.getArgumentsParameter().accept(this);
        methodDeclaration.getBody().accept(this);
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        this.elements.add(parameterDeclaration);

        this.elements.add(parameterDeclaration.getType());
    }

    @Override
    protected void visit(Statement.IfStatement statement, Void context) {
        this.elements.add(statement);

        statement.getCondition().accept(this);
        statement.getStatementIfTrue().accept(this);
        statement.getStatementIfFalse().ifPresent(s -> s.accept(this));
    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        this.elements.add(statement);

        statement.getCondition().accept(this);
        statement.getStatementWhileTrue().accept(this);
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
        this.elements.add(statement);

        statement.getExpression().accept(this);
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {
        this.elements.add(statement);

        statement.getValue().ifPresent(v -> v.accept(this));
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {
        this.elements.add(statement);
    }

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {
        this.elements.add(statement);

        this.elements.add(statement.getType());
        statement.getValue().ifPresent(v -> v.accept(this));
    }

    @Override
    protected void visit(Statement.Block block, Void context) {
        this.elements.add(block);

        block.getStatements().forEach(s -> s.accept(this));
    }

    @Override
    protected void visit(Expression.BinaryOperation expression, Void context) {
        this.elements.add(expression);

        this.elements.add(expression.getOperationType());
        expression.getLeft().accept(this);
        expression.getRight().accept(this);
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, Void context) {
        this.elements.add(expression);

        this.elements.add(expression.getOperationType());
        expression.getOther().accept(this);
    }

    @Override
    protected void visit(Expression.NullLiteral expression, Void context) {
        this.elements.add(expression);
    }

    @Override
    protected void visit(Expression.BooleanLiteral expression, Void context) {
        this.elements.add(expression);
    }

    @Override
    protected void visit(Expression.IntegerLiteral expression, Void context) {
        this.elements.add(expression);
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, Void context) {
        this.elements.add(expression);

        expression.getContext().ifPresent(e -> e.accept(this));
        this.elements.add(expression.getReference());
        expression.getArguments().forEach(a -> a.accept(this));
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Void context) {
        this.elements.add(expression);

        expression.getContext().accept(this);
        this.elements.add(expression.getReference());
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {
        this.elements.add(expression);

        expression.getContext().accept(this);
        expression.getIndex().accept(this);
    }

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {
        this.elements.add(expression);

        this.elements.add(expression.getReference());
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {
        this.elements.add(expression);
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        this.elements.add(expression);

        this.elements.add(expression.getReference());
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        this.elements.add(expression);

        this.elements.add(expression.getReference());
        expression.getPrimaryDimension().accept(this);
    }
}
