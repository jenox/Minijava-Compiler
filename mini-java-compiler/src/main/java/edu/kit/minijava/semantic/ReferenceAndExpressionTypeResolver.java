package edu.kit.minijava.semantic;

import java.util.Optional;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.TypeOfExpression;

/**
 * Because MiniJava allows use-before-declare, we need two passes: one for collecting class, method and fiel
 * declarations and one to resolve all references and expression types.
 *
 * Before visiting an expression, its type is not resolved. After visiting an expression, its type must be resolved.
 */
public class ReferenceAndExpressionTypeResolver extends SemanticAnalysisVisitorBase {
    public ReferenceAndExpressionTypeResolver(Program program) {
        program.accept(this, null);

        this.finishCollectingDeclarationsForUseBeforeDeclare();

        program.accept(this, null);

        assert this.getEntryPoint().isPresent() : "missing main method";
    }

    // MARK: - Traversal

    @Override
    protected void visit(Program program, Void context) {
        for (ClassDeclaration clsDecl : program.getClassDeclarations()) {
            clsDecl.accept(this, context);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare())
            this.registerClassDeclaration(classDeclaration);

        this.enterClassDeclaration(classDeclaration);

        for (FieldDeclaration fldDecl : classDeclaration.getFieldDeclarations()) {
            fldDecl.accept(this, context);
        }

        for (MethodDeclaration mthDecl : classDeclaration.getMethodDeclarations()) {
            mthDecl.accept(this, context);
        }

        for (MainMethodDeclaration mainMthdDecl : classDeclaration.getMainMethodDeclarations()) {
            mainMthdDecl.accept(this, context);
        }

        this.leaveCurrentClassDeclaration();
    }

    // TODO: wir duerfen keine Felder vom Typ `void` oder vom Typ `void[]` deklarieren
    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare())
            this.registerFieldDeclaration(fieldDeclaration, this.getCurrentClassDeclaration());
        else {
            fieldDeclaration.getType().accept(this, context);
            this.addVariableDeclarationToCurrentScope(fieldDeclaration);
        }
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare())
            this.registerMethodDeclaration(methodDeclaration, this.getCurrentClassDeclaration());
        else {
            methodDeclaration.getReturnType().accept(this, context);

            // TODO: ueberpruefen, dass return type nicht ein `void[]` ist

            for (VariableDeclaration paramDecl : methodDeclaration.getParameters())
                paramDecl.accept(this, context);

            this.enterMethodDeclaration(methodDeclaration);

            methodDeclaration.getBody().accept(this, context);

            // TODO: location zum assert-string hinzufuegen
            // TODO: return type zum assert-string hinzufuegen
            if (!methodDeclaration.getReturnType().isVoid())
                assert methodDeclaration.getBody()
                .explicitlyReturns() : "At least one path in the method body doesn't returns a value.";

                this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare())
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
        statement.getCondition().accept(this, context);
        assert statement.getCondition().getType().isBoolean() : "TODO";

        // visit branches
        // TODO: how to deal with return statements
        statement.getStatementIfTrue().accept(this, context);
        Optional<Statement> falseStmt = statement.getStatementIfFalse();
        if (falseStmt.isPresent()) {
            falseStmt.get().accept(this, context);
        }

    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        // check condition
        statement.getCondition().accept(this, context);
        assert statement.getCondition().getType().isBoolean();
        // visit child
        statement.getStatementWhileTrue().accept(this, context);
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
        statement.getExpression().accept(this, context);
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {
    }

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
        expression.getLeft().accept(this, context);
        expression.getRight().accept(this, context);

        TypeOfExpression left = expression.getLeft().getType();
        TypeOfExpression right = expression.getRight().getType();

        switch (expression.getOperationType()) {
            case ADDITION:
            case DIVISION:
            case MODULO:
            case MULTIPLICATION:
            case SUBTRACTION:
                assert expression.getLeft().getType().isInteger() : "should be int";
                assert expression.getRight().getType().isInteger() : "should be int";
                expression.getType().resolveToInteger();
                break;
            case LOGICAL_AND:
            case LOGICAL_OR:
                assert left.isBoolean();
                assert right.isBoolean();
                expression.getType().resolveToBoolean();
                break;
            case GREATER_THAN:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL_TO:
            case GREATER_THAN_OR_EQUAL_TO:
                assert left.isInteger() : "should be int";
                assert right.isInteger() : "should be int";
                expression.getType().resolveToBoolean();
                break;
            case EQUAL_TO:
            case NOT_EQUAL_TO:
                assert left.canCheckForEqualityWith(right);
                expression.getType().resolveToBoolean();
            case ASSIGNMENT:
                assert left.isAssignable() : "not assignable";
                assert right.isCompatibleWith(left) : "not compatible";
                expression.getType().resolveToTypeOfExpression(left, false);
            default:
                assert false : "should not happen";
        }
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, Void context) {
        expression.getOther().accept(this, context);

        switch (expression.getOperationType()) {
            case LOGICAL_NEGATION:
                assert expression.getType().isBoolean() : "expected boolean";
                expression.getType().resolveToBoolean();
                break;
            case NUMERIC_NEGATION:
                assert expression.getType().isInteger() : "expected int";
                expression.getType().resolveToInteger();
                break;
            default:
                assert false;
        }
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
        assert !(this.getCurrentMethodDeclaration() instanceof MainMethodDeclaration) : "";
        expression.getType().resolveToInstanceOfClass(this.getCurrentClassDeclaration(), false);

    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        String name = expression.getClassReference().getName();
        Optional<ClassDeclaration> classDecl = this.getClassDeclarationForName(name);
        assert classDecl.isPresent() : "";
        expression.getType().resolveToInstanceOfClass(classDecl.get(), false);
        expression.getClassReference().resolveTo(classDecl.get());
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        throw new UnsupportedOperationException();
    }
}
