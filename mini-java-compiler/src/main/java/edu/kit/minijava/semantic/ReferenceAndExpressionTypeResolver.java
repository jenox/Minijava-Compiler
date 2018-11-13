package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;


/**
 * Because MiniJava allows use-before-declare, we need two passes: one for collecting class, method and fiel
 * declarations and one to resolve all references and expression types.
 *
 * Before visiting an expression, its type is not resolved. After visiting an expression, its type must be resolved.
 */
public class ReferenceAndExpressionTypeResolver extends SemanticAnalysisVisitorBase {
    public ReferenceAndExpressionTypeResolver(Program program) {
        program.accept(this, null);

        assert this.getEntryPoint().isPresent() : "missing main method";
    }


    // MARK: - Traversal

    @Override
    protected void visit(Program program, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(ExplicitTypeReference reference, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(ImplicitTypeReference reference, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Statement.IfStatement statement, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {}

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Statement.Block block, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.BinaryOperation expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.NullLiteral expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.BooleanLiteral expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.IntegerLiteral expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        throw new UnsupportedOperationException();
    }
}
