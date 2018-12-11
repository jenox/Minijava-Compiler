package edu.kit.minijava.ast.nodes;

import java.util.*;

public abstract class ASTVisitor<T> {
    protected abstract void visit(Program program, T context);
    protected abstract void visit(ClassDeclaration classDeclaration, T context);
    protected abstract void visit(FieldDeclaration fieldDeclaration, T context);
    protected abstract void visit(MainMethodDeclaration methodDeclaration, T context);
    protected abstract void visit(MethodDeclaration methodDeclaration, T context);
    protected abstract void visit(ParameterDeclaration parameterDeclaration, T context);
    protected abstract void visit(ExplicitTypeReference reference, T context);
    protected abstract void visit(ImplicitTypeReference reference, T context);

    protected abstract void visit(Statement.IfStatement statement, T context);
    protected abstract void visit(Statement.WhileStatement statement, T context);
    protected abstract void visit(Statement.ExpressionStatement statement, T context);
    protected abstract void visit(Statement.ReturnStatement statement, T context);
    protected abstract void visit(Statement.EmptyStatement statement, T context);
    protected abstract void visit(Statement.LocalVariableDeclarationStatement statement, T context);
    protected abstract void visit(Statement.Block block, T context);

    protected abstract void visit(Expression.BinaryOperation expression, T context);
    protected abstract void visit(Expression.UnaryOperation expression, T context);
    protected abstract void visit(Expression.NullLiteral expression, T context);
    protected abstract void visit(Expression.BooleanLiteral expression, T context);
    protected abstract void visit(Expression.IntegerLiteral expression, T context);
    protected abstract void visit(Expression.MethodInvocation expression, T context);
    protected abstract void visit(Expression.ExplicitFieldAccess expression, T context);
    protected abstract void visit(Expression.ArrayElementAccess expression, T context);
    protected abstract void visit(Expression.VariableAccess expression, T context);
    protected abstract void visit(Expression.CurrentContextAccess expression, T context);
    protected abstract void visit(Expression.NewObjectCreation expression, T context);
    protected abstract void visit(Expression.NewArrayCreation expression, T context);
    protected abstract void visit(Expression.SystemOutPrintlnExpression expression, T context);
    protected abstract void visit(Expression.SystemOutFlushExpression expression, T context);
    protected abstract void visit(Expression.SystemOutWriteExpression expression, T context);
    protected abstract void visit(Expression.SystemInReadExpression expression, T context);

    private final Stack<ASTNode> nodes = new Stack<>();

    protected final void willVisit(ASTNode node) {
        this.nodes.push(node);
    }
    protected final void didVisit(ASTNode node) {
        assert this.nodes.peek() == node;

        this.nodes.pop();
    }

    protected final Optional<ASTNode> getPreviousNode() {
        return this.getPreviousNode(1);
    }

    protected final Optional<ASTNode> getPreviousNode(int offset) {
        if (this.nodes.size() >= offset + 1) {
            return Optional.of(this.nodes.get(this.nodes.size() - (offset + 1)));
        }
        else {
            return Optional.empty();
        }
    }
}
