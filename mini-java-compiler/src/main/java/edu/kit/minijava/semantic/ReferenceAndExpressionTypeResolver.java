package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;

public class ReferenceAndExpressionTypeResolver extends SemanticAnalysisVisitorBase {
    public ReferenceAndExpressionTypeResolver(Program program) {
        program.accept(this, null);

        assert this.getEntryPoint().isPresent() : "missing main method";
    }


    // MARK: - Traversal

    @Override
    protected void visit(Program program, Void context) {

        // Pass 1: collect declarations
        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this, context);
        }

        this.finishCollectingDeclarationsForUseBeforeDeclare();

        // Pass 2: resolve remaining types and references
        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this, context);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            this.registerClassDeclaration(classDeclaration);
        }

        this.enterClassDeclaration(classDeclaration);

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this, context);
        }

        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {
            methodDeclaration.accept(this, context);
        }

        for (MainMethodDeclaration methodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            methodDeclaration.accept(this, context);
        }

        this.leaveCurrentClassDeclaration();
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            this.registerFieldDeclaration(fieldDeclaration, this.getCurrentClassDeclaration());

            fieldDeclaration.getType().accept(this, context);
            // TODO: void or array of void not allowed here
        }
        else {
            this.addVariableDeclarationToCurrentScope(fieldDeclaration);
        }
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            this.registerMethodDeclaration(methodDeclaration, this.getCurrentClassDeclaration());

            methodDeclaration.getReturnType().accept(this, context);
            // TODO: array of void not allowed here
            methodDeclaration.getParameters().forEach(node -> node.accept(this, context));
        }
        else {
            this.enterMethodDeclaration(methodDeclaration);

            methodDeclaration.getParameters().forEach(node -> node.accept(this, context));
            methodDeclaration.getBody().accept(this, context);

            if (!methodDeclaration.getReturnType().isVoid()) {
                assert methodDeclaration.getBody().explicitlyReturns() : "must return a value on all paths";
            }

            assert !methodDeclaration.getBody().containsUnreachableStatements() : "contains unreachable statements";

            this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            methodDeclaration.getReturnType().accept(this, context);
            methodDeclaration.getArgumentsParameter().accept(this, context);

            this.setEntryPoint(methodDeclaration);
        }
        else {
            this.enterMethodDeclaration(methodDeclaration);

            methodDeclaration.getReturnType().accept(this, context);
            methodDeclaration.getArgumentsParameter().accept(this, context);
            methodDeclaration.getBody().accept(this, context);

            this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        if (this.isCollectingDeclarationsForUseBeforeDeclare()) {
            parameterDeclaration.getType().accept(this, context);
            // TODO: void or array of void not allowed here
        }
        else {
            this.addVariableDeclarationToCurrentScope(parameterDeclaration);
        }
    }

    @Override
    protected void visit(ExplicitTypeReference reference, Void context) {
        String typeName = reference.getBasicTypeReference().getName();
        Optional<BasicTypeDeclaration> typeDeclaration = this.getBasicTypeNamed(typeName);
        assert typeDeclaration.isPresent() : "use of undeclared type";

        reference.getBasicTypeReference().resolveTo(typeDeclaration.get());
    }

    @Override
    protected void visit(ImplicitTypeReference reference, Void context) {
        // noop
    }

    @Override
    protected void visit(Statement.IfStatement statement, Void context) {
        statement.getCondition().accept(this, context);

        assert statement.getCondition().getType().isBoolean() : "condition for if statement must be boolean";

        statement.getStatementIfTrue().accept(this, context);
        statement.getStatementIfFalse().ifPresent(node -> node.accept(this, context));
    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        statement.getCondition().accept(this, context);

        assert statement.getCondition().getType().isBoolean() : "condition for while statement must be boolean";

        statement.getStatementWhileTrue().accept(this, context);
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
        statement.getExpression().accept(this, context);

        assert statement.getExpression().isValidForExpressionStatement() : "not a statement";
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {
        statement.getValue().ifPresent(node -> node.accept(this, context));

        TypeReference expectedReturnType = this.getCurrentMethodDeclaration().getReturnType();

        if (statement.getValue().isPresent()) {
            TypeOfExpression actualReturnType = statement.getValue().get().getType();

            assert actualReturnType.isCompatibleWith(expectedReturnType) : "invalid return value";
        }
        else {
            assert expectedReturnType.isVoid() : "must return value from non-void function";
        }
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {}

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {

        // Resolve type.
        statement.getType().accept(this, context);
        // TODO: void or array of void not allowed here

        // TODO: Is `int x = x + 1` valid if `x` isn't a field? Definite Assignment? If it is a field, which `x` should
        // TODO: we pick?
        this.addVariableDeclarationToCurrentScope(statement);

        // Ensure type of value matches (if present).
        if (statement.getValue().isPresent()) {
            statement.getValue().get().accept(this, context);

            assert statement.getValue().get().getType().isCompatibleWith(statement.getType()) :
                    "incompatible value for variable declaration";
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


    // MARK: - Expressions

    // Before visiting an expression, its type is not resolved. After visiting an expression, its type must be resolved.

    @Override
    protected void visit(Expression.BinaryOperation expression, Void context) {
        expression.getLeft().accept(this, context);
        expression.getRight().accept(this, context);

        switch (expression.getOperationType()) {
            case MULTIPLICATION:
            case DIVISION:
            case MODULO:
            case ADDITION:
            case SUBTRACTION:
                assert expression.getLeft().getType().isInteger() : "can only use numeric operations on integers";
                assert expression.getRight().getType().isInteger() : "can only use numeric operations on integers";
                expression.getType().resolveToInteger();
                break;
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL_TO:
                assert expression.getLeft().getType().isInteger() : "can only use comparison operations on integers";
                assert expression.getRight().getType().isInteger() : "can only use comparison operations on integers";
                expression.getType().resolveToBoolean();
                break;
            case EQUAL_TO:
            case NOT_EQUAL_TO:
                assert expression.getRight().getType().canCheckForEqualityWith(expression.getLeft().getType()) :
                        "incompatible operand types for equality check";
                expression.getType().resolveToBoolean();
                break;
            case LOGICAL_AND:
            case LOGICAL_OR:
                assert expression.getLeft().getType().isBoolean() : "can only use logical operations on booleans";
                assert expression.getRight().getType().isBoolean() : "can only use logical operations on booleans";
                expression.getType().resolveToBoolean();
                break;
            case ASSIGNMENT:
                assert expression.getRight().getType().isCompatibleWith(expression.getLeft().getType()) :
                        "incompatible operand types for assignment" + expression.getLeft() + expression.getRight();
                assert expression.getLeft().getType().isAssignable() : "cannot assign rvalue";
                expression.getType().resolveTo(expression.getLeft().getType(), false);
                break;
        }
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, Void context) {
        expression.getOther().accept(this, context);

        switch (expression.getOperationType()) {
            case LOGICAL_NEGATION:
                assert expression.getOther().getType().isBoolean() : "can only use logical negation on booleans";

                expression.getType().resolveToBoolean();

                break;
            case NUMERIC_NEGATION:
                assert expression.getOther().getType().isInteger() : "can only use numeric negation on integers";

                expression.getType().resolveToInteger();

                break;
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
        try {
            Integer.parseInt(expression.getValue());
        }
        catch (NumberFormatException exception) {
            assert false : "integer too big";
        }

        expression.getType().resolveToInteger();
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, Void context) {
        expression.getContext().ifPresent(node -> node.accept(this, context));
        expression.getArguments().forEach(node -> node.accept(this, context));

        String methodName = expression.getMethodReference().getName();
        Optional<MethodDeclaration> methodDeclaration;

        if (expression.getContext().isPresent()) {
            TypeOfExpression typeOfContext = expression.getContext().get().getType();

            assert typeOfContext.getDeclaration().isPresent() : "cannot access method on expression of type null";
            assert typeOfContext.getNumberOfDimensions() == 0 : "cannot access method on array";

            BasicTypeDeclaration typeDeclaration = typeOfContext.getDeclaration().get();

            assert typeDeclaration instanceof ClassDeclaration : "can only access methods on objects";

            methodDeclaration = this.getMethodDeclarationForName(methodName, (ClassDeclaration)typeDeclaration);
        }
        else {
            assert !(this.getCurrentMethodDeclaration() instanceof MainMethodDeclaration) :
                    "must not invoke methods on implicit this in main method";

            methodDeclaration = this.getMethodDeclarationForName(methodName, this.getCurrentClassDeclaration());
        }

        assert methodDeclaration.isPresent() : "use of undeclared method";

        List<TypeOfExpression> argumentTypes = expression.getMethodReference().getArgumentTypes();
        List<TypeReference> parameterTypes = methodDeclaration.get().getParameterTypes();

        assert argumentTypes.size() == parameterTypes.size() : "incorrect number of arguments";
        for (int index = 0; index < argumentTypes.size(); index += 1) {
            assert argumentTypes.get(index).isCompatibleWith(parameterTypes.get(index)) : "incompatible argument type";
        }

        expression.getMethodReference().resolveTo(methodDeclaration.get());
        expression.getType().resolveTo(methodDeclaration.get().getReturnType(), false);
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Void context) {
        expression.getContext().accept(this, context);

        TypeOfExpression typeOfContext = expression.getContext().getType();

        assert typeOfContext.getDeclaration().isPresent() : "cannot access field on expression of type null";
        assert typeOfContext.getNumberOfDimensions() == 0 : "cannot access field on array";

        BasicTypeDeclaration typeDeclaration = typeOfContext.getDeclaration().get();

        assert typeDeclaration instanceof ClassDeclaration : "can only access fields on objects";

        ClassDeclaration classDeclaration = (ClassDeclaration)typeDeclaration;
        String fieldName = expression.getFieldReference().getName();

        Optional<FieldDeclaration> fieldDeclaration = this.getFieldDeclarationForName(fieldName, classDeclaration);

        assert fieldDeclaration.isPresent() : "use of undeclared field";

        expression.getFieldReference().resolveTo(fieldDeclaration.get());
        expression.getType().resolveTo(fieldDeclaration.get().getType(), true);
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {
        expression.getContext().accept(this, context);
        expression.getIndex().accept(this, context);

        TypeOfExpression type = expression.getContext().getType();
        assert type.getDeclaration().isPresent() : "context of array access must be array";
        assert type.getNumberOfDimensions() >= 1 : "context of array access must be array";

        expression.getType().resolveTo(type.getDeclaration().get(), type.getNumberOfDimensions() - 1, true);
    }

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {
        String name = expression.getVariableReference().getName();
        Optional<VariableDeclaration> declaration = this.getVariableDeclarationForName(name);

        assert declaration.isPresent() : "use of undeclared variable " + name;
        assert declaration.get().canBeAccessed() : "variable may not be accessed. sorry.";

        expression.getVariableReference().resolveTo(declaration.get());
        expression.getType().resolveTo(declaration.get().getType(), true);
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {
        assert !(this.getCurrentMethodDeclaration() instanceof MainMethodDeclaration) :
                "this must not be accessed in main method";

        expression.getType().resolveTo(this.getCurrentClassDeclaration(), false);
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        String className = expression.getClassReference().getName();
        Optional<ClassDeclaration> classDeclaration = this.getClassDeclarationForName(className);

        assert classDeclaration.isPresent() : "use of undeclared class";

        expression.getClassReference().resolveTo(classDeclaration.get());
        expression.getType().resolveTo(classDeclaration.get(), false);
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        expression.getPrimaryDimension().accept(this, context);

        TypeOfExpression primaryDimensionType = expression.getPrimaryDimension().getType();
        assert primaryDimensionType.getDeclaration().isPresent() : "dim must be int";
        assert primaryDimensionType.getDeclaration().get() == PrimitiveTypeDeclaration.INTEGER : "dim must be int";
        assert primaryDimensionType.getNumberOfDimensions() == 0 : "dim must be int";

        String name = expression.getBasicTypeReference().getName();
        Optional<BasicTypeDeclaration> typeDeclaration = this.getBasicTypeNamed(name);
        assert typeDeclaration.isPresent() : "use of undeclared type in new array";
        assert typeDeclaration.get() != PrimitiveTypeDeclaration.VOID : "array of void is not allowed";

        expression.getBasicTypeReference().resolveTo(typeDeclaration.get());
        expression.getType().resolveTo(typeDeclaration.get(), expression.getNumberOfDimensions(), false);
    }


    // MARK: - Helpers

    private Optional<BasicTypeDeclaration> getBasicTypeNamed(String name) {
        for (PrimitiveTypeDeclaration type : PrimitiveTypeDeclaration.values()) {
            if (name.equals(type.getName()) && type.canBeReferencedByUser()) {
                return Optional.of(type);
            }
        }

        return Optional.ofNullable(this.getClassDeclarationForName(name).orElse(null));
    }
}
