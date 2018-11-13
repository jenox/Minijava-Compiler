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

        finishCollectingDeclarationsForUseBeforeDeclare();

        program.accept(this, null);


        assert this.getEntryPoint().isPresent() : "missing main method";
    }


    // MARK: - Traversal

    @Override
    protected void visit(Program program, Void context) {
        for(ClassDeclaration clsDecl : program.getClassDeclarations())
            clsDecl.accept(this, context);
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        if(this.isCollectingDeclarationsForUseBeforeDeclare())
            this.registerClassDeclaration(classDeclaration);

        this.enterClassDeclaration(classDeclaration);

        for(FieldDeclaration fldDecl : classDeclaration.getFieldDeclarations())
            fldDecl.accept(this, context);

        for(MethodDeclaration mthDecl : classDeclaration.getMethodDeclarations())
            mthDecl.accept(this, context);

        for(MainMethodDeclaration mainMthdDecl : classDeclaration.getMainMethodDeclarations())
            mainMthdDecl.accept(this, context);

        this.leaveCurrentClassDeclaration();
    }

    // TODO: wir duerfen keine Felder vom Typ `void` oder vom Typ `void[]` deklarieren
    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        if(this.isCollectingDeclarationsForUseBeforeDeclare())
            this.registerFieldDeclaration(fieldDeclaration, this.getCurrentClassDeclaration());
        else {
           fieldDeclaration.getType().accept(this, context);
           this.addVariableDeclarationToCurrentScope(fieldDeclaration);
        }
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        if(this.isCollectingDeclarationsForUseBeforeDeclare())
            this.registerMethodDeclaration(methodDeclaration, this.getCurrentClassDeclaration());
        else {
            methodDeclaration.getReturnType().accept(this, context);


            // TODO: ueberpruefen, dass return type nicht ein `void[]` ist

            for(VariableDeclaration paramDecl : methodDeclaration.getParameters())
                paramDecl.accept(this, context);

            this.enterMethodDeclaration(methodDeclaration);

            methodDeclaration.getBody().accept(this, context);

            // TODO: location zum assert-string hinzufuegen
            // TODO: return type zum assert-string hinzufuegen
            if(!methodDeclaration.getReturnType().isVoid())
                assert methodDeclaration.getBody().explicitlyReturns() : "At least one path in the method body doesn't returns a value.";

            this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        if(isCollectingDeclarationsForUseBeforeDeclare())
            this.setEntryPoint(methodDeclaration);
        else {
            methodDeclaration.getReturnType().accept(this, context);
            methodDeclaration.getArgumentsParameter().accept(this, context);


            this.enterMethodDeclaration(methodDeclaration);
            methodDeclaration.getBody().accept(this, context);
            this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        parameterDeclaration.getType().accept(this, context);

        // TODO: checken, dass Typ nicht `void` oder `void[]` ist

        this.addVariableDeclarationToCurrentScope(parameterDeclaration);
    }

    @Override
    protected void visit(ExplicitTypeReference reference, Void context) {
        String name = reference.getBasicTypeReference().getName();
        assert this.getBasicTypeDeclarationForName(name).isPresent() : "Use of undeclared type: `" + name + "`";

        reference.getBasicTypeReference().resolveTo(this.getBasicTypeDeclarationForName(name).get());
    }

    @Override
    protected void visit(ImplicitTypeReference reference, Void context) {
        // NO-OP
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
        expression.getType().resolveToNull();
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
