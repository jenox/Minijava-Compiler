package edu.kit.minijava.cli;

import edu.kit.minijava.ast.nodes.*;

public final class ASTDumper extends ASTDumperBase {

    public ASTDumper(Program program) {
        super(program);
    }

    @Override
    protected void visit(Program program, Void context) {
        this.outputBasicTypeDeclarationNode(PrimitiveTypeDeclaration.VOID);
        this.outputBasicTypeDeclarationNode(PrimitiveTypeDeclaration.INTEGER);
        this.outputBasicTypeDeclarationNode(PrimitiveTypeDeclaration.BOOLEAN);
        this.outputBasicTypeDeclarationNode(PrimitiveTypeDeclaration.STRING);

        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        this.outputBasicTypeDeclarationNode(classDeclaration);

        for (MainMethodDeclaration methodDeclaration: classDeclaration.getMainMethodDeclarations()) {
            this.outputOwningEdgeAndVisit(classDeclaration, methodDeclaration);
        }

        for (MethodDeclaration methodDeclaration: classDeclaration.getMethodDeclarations()) {
            this.outputOwningEdgeAndVisit(classDeclaration, methodDeclaration);
        }

        for (FieldDeclaration fieldDeclaration: classDeclaration.getFieldDeclarations()) {
            this.outputOwningEdgeAndVisit(classDeclaration, fieldDeclaration);
        }
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        this.outputVariableDeclarationNode(fieldDeclaration);

        this.outputOwningEdgeAndVisit(fieldDeclaration, fieldDeclaration.getType());
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        this.outputSubroutineDeclarationNode(methodDeclaration);

        this.outputOwningEdgeAndVisit(methodDeclaration, methodDeclaration.getReturnType());
        this.outputOwningEdgeAndVisit(methodDeclaration, methodDeclaration.getArgumentsParameter());
        this.outputOwningEdgeAndVisit(methodDeclaration, methodDeclaration.getBody());
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        this.outputSubroutineDeclarationNode(methodDeclaration);

        this.outputOwningEdgeAndVisit(methodDeclaration, methodDeclaration.getReturnType());

        for (VariableDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
            this.outputOwningEdgeAndVisit(methodDeclaration, parameterDeclaration);
        }

        this.outputOwningEdgeAndVisit(methodDeclaration, methodDeclaration.getBody());
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        this.outputVariableDeclarationNode(parameterDeclaration);

        this.outputOwningEdgeAndVisit(parameterDeclaration, parameterDeclaration.getType());
    }

    @Override
    protected void visit(ExplicitTypeReference reference, Void context) {
        this.outputReferenceNode(reference);

        this.outputReferencingEdge(reference, reference.getBasicTypeReference().getDeclaration());
    }

    @Override
    protected void visit(ImplicitTypeReference reference, Void context) {
        this.outputReferenceNode(reference);

        this.outputReferencingEdge(reference, reference.getBasicTypeReference().getDeclaration());
    }

    @Override
    protected void visit(Statement.IfStatement statement, Void context) {
        this.outputStatementNode(statement);

        this.outputOwningEdgeAndVisit(statement, statement.getCondition());
        this.outputOwningEdgeAndVisit(statement, statement.getStatementIfTrue());
        this.outputOwningEdgeAndVisit(statement, statement.getStatementIfFalse().orElse(null));
    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        this.outputStatementNode(statement);

        this.outputOwningEdgeAndVisit(statement, statement.getCondition());
        this.outputOwningEdgeAndVisit(statement, statement.getStatementWhileTrue());
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
        this.outputStatementNode(statement);

        this.outputOwningEdgeAndVisit(statement, statement.getExpression());
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {
        this.outputStatementNode(statement);

        this.outputOwningEdgeAndVisit(statement, statement.getValue().orElse(null));
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {
        this.outputStatementNode(statement);
    }

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {
        this.outputVariableDeclarationNode(statement);

        this.outputOwningEdgeAndVisit(statement, statement.getType());
        this.outputOwningEdgeAndVisit(statement, statement.getValue().orElse(null));
    }

    @Override
    protected void visit(Statement.Block block, Void context) {
        this.outputStatementNode(block);

        for (Statement statement : block.getStatements()) {
            this.outputOwningEdgeAndVisit(block, statement);
        }
    }

    @Override
    protected void visit(Expression.BinaryOperation expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndVisit(expression, expression.getLeft());
        this.outputOwningEdgeAndVisit(expression, expression.getRight());
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndVisit(expression, expression.getOther());
    }

    @Override
    protected void visit(Expression.NullLiteral expression, Void context) {
        this.outputExpressionNode(expression);
    }

    @Override
    protected void visit(Expression.BooleanLiteral expression, Void context) {
        this.outputExpressionNode(expression);
    }

    @Override
    protected void visit(Expression.IntegerLiteral expression, Void context) {
        this.outputExpressionNode(expression);
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndVisit(expression, expression.getContext().orElse(null));
        this.outputOwningEdgeAndResolve(expression, expression.getMethodReference());

        for (Expression argument : expression.getArguments()) {
            this.outputOwningEdgeAndVisit(expression, argument);
        }
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndVisit(expression, expression.getContext());
        this.outputOwningEdgeAndResolve(expression, expression.getFieldReference());
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndVisit(expression, expression.getContext());
        this.outputOwningEdgeAndVisit(expression, expression.getIndex());
    }

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndResolve(expression, expression.getVariableReference());
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {
        this.outputExpressionNode(expression);
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndResolve(expression, expression.getClassReference());
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        this.outputExpressionNode(expression);

        this.outputOwningEdgeAndResolve(expression, expression.getBasicTypeReference());
        this.outputOwningEdgeAndVisit(expression, expression.getPrimaryDimension());
    }

    @Override
    protected void visit(Expression.SystemOutPrintlnExpression expression, Void context) {
        this.outputSystemCallNode(expression);

        this.outputOwningEdgeAndVisit(expression, expression.getArgument());
    }

    @Override
    protected void visit(Expression.SystemOutFlushExpression expression, Void context) {
        this.outputSystemCallNode(expression);
    }

    @Override
    protected void visit(Expression.SystemOutWriteExpression expression, Void context) {
        this.outputSystemCallNode(expression);

        this.outputOwningEdgeAndVisit(expression, expression.getArgument());
    }

    @Override
    protected void visit(Expression.SystemInReadExpression expression, Void context) {
        this.outputSystemCallNode(expression);
    }
}
