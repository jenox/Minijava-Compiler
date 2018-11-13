package edu.kit.minijava.semantic;

import java.util.*;

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
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            this.registerClassDeclaration(classDeclaration);
        }

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
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            this.registerFieldDeclaration(fieldDeclaration, this.getCurrentClassDeclaration());
        }
        else {
            fieldDeclaration.getType().accept(this, context);
            this.addVariableDeclarationToCurrentScope(fieldDeclaration);
        }
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            this.registerMethodDeclaration(methodDeclaration, this.getCurrentClassDeclaration());
        }
        else {
            methodDeclaration.getReturnType().accept(this, context);

            // TODO: ueberpruefen, dass return type nicht ein `void[]` ist

            for (VariableDeclaration paramDecl : methodDeclaration.getParameters()) {
                paramDecl.accept(this, context);
            }

            this.enterMethodDeclaration(methodDeclaration);

            methodDeclaration.getBody().accept(this, context);

            // TODO: location zum assert-string hinzufuegen
            // TODO: return type zum assert-string hinzufuegen
            if (!methodDeclaration.getReturnType().isVoid()) {
                assert methodDeclaration.getBody()
                        .explicitlyReturns() : "At least one path in the method body doesn't returns a value.";
            }

            this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            this.setEntryPoint(methodDeclaration);
        }
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
        this.enterNewVariableDeclarationScope();
        statement.getStatementIfTrue().accept(this, context);
        this.leaveCurrentVariableDeclarationScope();

        Optional<Statement> falseStmt = statement.getStatementIfFalse();
        if (falseStmt.isPresent()) {
            this.enterNewVariableDeclarationScope();
            falseStmt.get().accept(this, context);
            this.leaveCurrentVariableDeclarationScope();
        }

    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        // check condition
        statement.getCondition().accept(this, context);
        assert statement.getCondition().getType().isBoolean();
        // visit child
        this.enterNewVariableDeclarationScope();
        statement.getStatementWhileTrue().accept(this, context);
        this.leaveCurrentVariableDeclarationScope();
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
        statement.getExpression().accept(this, context);
        // TODO: check for not-a-statement
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {
        // get method return type
        Optional<Expression> returnValue = statement.getValue();
        TypeReference returnType = this.getCurrentMethodDeclaration().getReturnType();
        if (returnType.isVoid()) {
            assert !returnValue.isPresent() : "TODO";
        }

    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {
        // NO-OP
    }

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {
        // add variable to symbol table
        statement.getType().accept(this, context);
        this.addVariableDeclarationToCurrentScope(statement);
        Optional<Expression> value = statement.getValue();
        if (value.isPresent()) {
            value.get().accept(this, context);
            canAssignTypeOfExpressionToTypeReference(value.get().getType(), statement.getType());
        }

    }

    @Override
    protected void visit(Statement.Block block, Void context) {
        this.enterNewVariableDeclarationScope();
        for (Statement statement : block.getStatements()) {
            statement.accept(this, context);
        }
        this.leaveCurrentVariableDeclarationScope();
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
                assert canCheckForEqualityWithTypesOfExpressions(left, right);
                expression.getType().resolveToBoolean();
            case ASSIGNMENT:
                assert left.isAssignable() : "not assignable";
                assert canAssignTypeOfExpressionToTypeOfExpression(right, left) : "not compatible";
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
        expression.getType().resolveToBoolean();
    }

    @Override
    protected void visit(Expression.IntegerLiteral expression, Void context) {
        // TODO: check if valid integer
        expression.getType().resolveToInteger();
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, Void context) {
        Optional<Expression> mContext = expression.getContext();
        ClassDeclaration classDecl = null;
        if (mContext.isPresent()) {
            mContext.get().accept(this, context);
            TypeOfExpression expType = mContext.get().getType();
            // TODO: set classDecl
            assert !expType.isNull() : "TODO";
            assert expType.isArrayType() : "TODO";
        }
        else {
            classDecl = this.getCurrentClassDeclaration();
        }

        // resolve declaration
        Optional<MethodDeclaration> mDecl = this.getMethodDeclarationForName(expression.getMethodReference().getName(),
                classDecl);
        assert mDecl.isPresent();
        expression.getMethodReference().resolveTo(mDecl.get());

        // check parameter
        List<? extends VariableDeclaration> params = expression.getMethodReference().getDeclaration().getParameters();
        List<Expression> args = expression.getArguments();
        assert params.size() == args.size() : "TODO";

        for (int i = 0; i < params.size(); i++) {
            Expression paramExp = args.get(i);
            TypeReference paramType = params.get(i).getType();
            paramExp.accept(this, context);
            canAssignTypeOfExpressionToTypeReference(paramExp.getType(), paramType);
        }

        TypeReference returnType = expression.getMethodReference().getDeclaration().getReturnType();
        expression.getType().resolveToTypeReference(returnType, false);

        // TODO: check that method is not static

    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Void context) {
        // resolve field declaration
        ClassDeclaration classDecl = null;
        String name = expression.getFieldReference().getName();
        expression.getContext().accept(this, context);
        // TODO: set classDecl and name
        Optional<FieldDeclaration> fieldDecl = this.getFieldDeclarationForName(name, classDecl);
        assert fieldDecl.isPresent() : "TODO";
        expression.getFieldReference().resolveTo(fieldDecl.get());

        assert fieldDecl.get().canBeAccessed() : "TODO";
        assert fieldDecl.get().getType().getNumberOfDimensions() == 0 : "accesing arrayType";
        expression.getType().resolveToTypeReference(fieldDecl.get().getType(), true);// TODO: assignable = true?
        // TODO: missing checks?
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {
        expression.getContext().accept(this, context);
        assert expression.getContext().getType().isArrayType() : "TODO";

        // check index
        expression.getIndex().accept(this, context);
        assert expression.getIndex().getType().isInteger() : "TODO";

        // set expression type
        // TODO: declaration alreade set?
        Optional<BasicTypeDeclaration> decl = expression.getContext().getType().getDeclaration();
        int numOfDims = expression.getContext().getType().getNumberOfDimensions() - 1;
        expression.getType().resolveToArrayOf(decl.get(), numOfDims, true); // assignable = true?

    }

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {
        String name = expression.getVariableReference().getName();
        Optional<VariableDeclaration> var = this.getVariableDeclarationForName(name);
        assert var.isPresent() : "TODO";
        expression.getVariableReference().resolveTo(var.get());
        expression.getType().resolveToTypeReference(var.get().getType(), true);

        //TODO: check that variable can be accessed
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {
        assert !(this.getCurrentMethodDeclaration() instanceof MainMethodDeclaration) : "TODO";
        expression.getType().resolveToInstanceOfClass(this.getCurrentClassDeclaration(), false);

    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        String name = expression.getClassReference().getName();
        Optional<ClassDeclaration> classDecl = this.getClassDeclarationForName(name);
        assert classDecl.isPresent() : "TODO";
        expression.getType().resolveToInstanceOfClass(classDecl.get(), false);
        expression.getClassReference().resolveTo(classDecl.get());
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        throw new UnsupportedOperationException();
    }
}
