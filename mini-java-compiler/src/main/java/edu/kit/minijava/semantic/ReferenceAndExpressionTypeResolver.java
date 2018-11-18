package edu.kit.minijava.semantic;

import java.util.*;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

/**
 * Because MiniJava allows use-before-declare, we need two passes: one for collecting class, method and field
 * declarations and one to resolve all references and expression types.
 *
 * Before visiting an expression, its type is not resolved. After visiting an expression, its type must be resolved.
 */
public class ReferenceAndExpressionTypeResolver extends SemanticAnalysisVisitorBase {
    public ReferenceAndExpressionTypeResolver(Program program) throws SemanticException {
        this.enterNewVariableDeclarationScope();

        // Install standard library declarations.
        this.addGlobalVariableDeclaration(CompilerMagic.SYSTEM_VARIABLE);
        this.registerClassDeclaration(CompilerMagic.SYSTEM);
        this.registerClassDeclaration(CompilerMagic.SYSTEM_IN);
        this.registerClassDeclaration(CompilerMagic.SYSTEM_OUT);
        this.registerFieldDeclaration(CompilerMagic.IN, CompilerMagic.SYSTEM);
        this.registerFieldDeclaration(CompilerMagic.OUT, CompilerMagic.SYSTEM);
        this.registerMethodDeclaration(CompilerMagic.PRINTLN, CompilerMagic.SYSTEM_OUT);
        this.registerMethodDeclaration(CompilerMagic.FLUSH, CompilerMagic.SYSTEM_OUT);
        this.registerMethodDeclaration(CompilerMagic.WRITE, CompilerMagic.SYSTEM_OUT);
        this.registerMethodDeclaration(CompilerMagic.READ, CompilerMagic.SYSTEM_IN);

        // Catch semantic exceptions which are wrapped in unchecked exceptions to not break
        // visitor pattern and stream handling.
        try {
            // First pass: collect classes
            program.accept(this, null);
            this.finishCollectingClassDeclarations();

            // Second pass: collect class members (fields/methods)
            program.accept(this, null);
            this.finishCollectingClassMemberDeclarations();

            // Third pass: check declarations
            program.accept(this, null);
        }
        catch (WrappedSemanticException exception) {
            // Unpack wrapped exception and rethrow as checked exception
            throw exception.getException();
        }

        if (!this.getEntryPoint().isPresent()) {
            throw new SemanticException("Missing main method");
        }

        this.leaveCurrentVariableDeclarationScope();
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
        if (this.isCollectingClassDeclarations()) {
            this.registerClassDeclaration(classDeclaration);
        }
        else {
            this.enterClassDeclaration(classDeclaration);

            classDeclaration.getFieldDeclarations().forEach(node -> node.accept(this, context));
            classDeclaration.getMethodDeclarations().forEach(node -> node.accept(this, context));
            classDeclaration.getMainMethodDeclarations().forEach(node -> node.accept(this, context));

            this.leaveCurrentClassDeclaration();
        }
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        if (this.isCollectingClassMemberDeclarations()) {
            fieldDeclaration.getType().accept(this, context);

            // Field types must not be of type void or array of void.
            if (fieldDeclaration.getType().isVoid() || fieldDeclaration.getType().isDimensionalVoid()) {
                throw fail(new SemanticException("Field type must not be void or array of void",
                    fieldDeclaration.toString()));
            }

            this.registerFieldDeclaration(fieldDeclaration, this.getCurrentClassDeclaration());
        }
        else {
            this.addVariableDeclarationToCurrentScope(fieldDeclaration);
        }
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingClassMemberDeclarations()) {
            this.enterMethodDeclaration(methodDeclaration);

            methodDeclaration.getReturnType().accept(this, context);

            // Return type must not be array of void.
            if (methodDeclaration.getReturnType().isDimensionalVoid()) {
                throw fail(new SemanticException("Method must not return array of void", methodDeclaration.toString()));
            }

            methodDeclaration.getParameters().forEach(node -> node.accept(this, context));

            this.leaveCurrentMethodDeclaration();
            this.registerMethodDeclaration(methodDeclaration, this.getCurrentClassDeclaration());
        }
        else {
            this.enterMethodDeclaration(methodDeclaration);

            // Parameter types are already resolved, but we need to add the variable declarations to current scope.
            methodDeclaration.getParameters().forEach(node -> node.accept(this, context));

            methodDeclaration.getBody().accept(this, context);

            // Non-void methods must return a value.
            if (!methodDeclaration.getReturnType().isVoid() && !methodDeclaration.getBody().explicitlyReturns()) {
                throw fail(new SemanticException("Method must return a value", methodDeclaration.toString()));
            }

            // NOTE: At this point, we could also check for unreachable code in the method.
            // However, the MiniJava language specification does not permit the rejection of programs that contain
            // unreachable code as required by the Java specification.

            this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        if (this.isCollectingClassMemberDeclarations()) {
            this.enterMethodDeclaration(methodDeclaration);

            // Main method must be named main.
            if (!methodDeclaration.getName().equals("main")) {
                throw fail(new SemanticException("Entry point must be named main", methodDeclaration.toString()));
            }

            methodDeclaration.getReturnType().accept(this, context);

            // Main method must return void, but AST doesn't guarantee it.
            if (!methodDeclaration.getReturnType().isVoid()) {
                throw fail(new SemanticException("Main method must return void", methodDeclaration.toString()));
            }

            methodDeclaration.getArgumentsParameter().accept(this, context);

            // Main method must take array of strings, but AST doesn't guarantee it.
            if (!methodDeclaration.getArgumentsParameter().getType().isArrayOfString()) {
                throw fail(new SemanticException("Main method expects String[] as parameter type",
                                                 methodDeclaration.toString()));
            }

            this.leaveCurrentMethodDeclaration();

            this.setEntryPoint(methodDeclaration);
        }
        else {
            this.enterMethodDeclaration(methodDeclaration);

            // Parameter types are already resolved, but we need to add the variable declarations to current scope.
            methodDeclaration.getArgumentsParameter().accept(this, context);

            methodDeclaration.getBody().accept(this, context);

            this.leaveCurrentMethodDeclaration();
        }
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        if (this.isCollectingClassMemberDeclarations()) {
            parameterDeclaration.getType().accept(this, context);

            // Parameter types must not be of type void or array of void.
            if (parameterDeclaration.getType().isVoid() || parameterDeclaration.getType().isDimensionalVoid()) {
                throw fail(new SemanticException("Method parameter type must not be void or array of void",
                    parameterDeclaration.toString()));
            }
        }
        else {
            this.addVariableDeclarationToCurrentScope(parameterDeclaration);
        }
    }

    @Override
    protected void visit(ExplicitTypeReference reference, Void context) {
        String name = reference.getBasicTypeReference().getName();
        Optional<BasicTypeDeclaration> declaration = this.getBasicTypeDeclarationForName(name);

        // Type reference must be resolvable.
        if (!declaration.isPresent()) {
            throw fail(new SemanticException("Use of undeclared type '" + name + "'", null,
                reference.getBasicTypeReference().getLocation()));
        }

        reference.getBasicTypeReference().resolveTo(declaration.get());
    }

    @Override
    protected void visit(ImplicitTypeReference reference, Void context) {
        // Implicit references are already resolved upon creation.
    }

    @Override
    protected void visit(Statement.IfStatement statement, Void context) {
        statement.getCondition().accept(this, context);

        // Condition for if statement must be boolean.
        if (!statement.getCondition().getType().isBoolean()) {
            throw fail(new TypeMismatchException(statement.getCondition().getType().toString(),
                statement.getCondition().getLocation(),
                "condition for if statement", null, "boolean"));
        }

        // AST doesn't guarantee child statements not being local variable declaration, only ASTs vended from Parser do.
        this.enterNewVariableDeclarationScope();
        statement.getStatementIfTrue().accept(this, context);
        this.leaveCurrentVariableDeclarationScope();
        this.enterNewVariableDeclarationScope();
        statement.getStatementIfFalse().ifPresent(node -> node.accept(this, context));
        this.leaveCurrentVariableDeclarationScope();
    }

    @Override
    protected void visit(Statement.WhileStatement statement, Void context) {
        statement.getCondition().accept(this, context);

        // Condition for while statement must be boolean.
        if (!statement.getCondition().getType().isBoolean()) {
            throw fail(new TypeMismatchException(statement.getCondition().getType().toString(),
                statement.getCondition().getLocation(),
                "condition for while statement", null, "boolean"));
        }

        // AST doesn't guarantee child statements not being local variable declaration, only ASTs vended from Parser do.
        this.enterNewVariableDeclarationScope();
        statement.getStatementWhileTrue().accept(this, context);
        this.leaveCurrentVariableDeclarationScope();
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Void context) {
        statement.getExpression().accept(this, context);

        // Expression must be valid for expression statement.
        if (!statement.getExpression().isValidForExpressionStatement()) {
            throw fail(new SemanticException("Not a statement", "in " + this.getCurrentMethodDeclaration().toString(),
                statement.getExpression().getLocation()));
        }
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Void context) {
        statement.getValue().ifPresent(node -> node.accept(this, context));

        TypeReference expectedReturnType = this.getCurrentMethodDeclaration().getReturnType();

        if (statement.getValue().isPresent()) {
            TypeOfExpression actualReturnType = statement.getValue().get().getType();

            // Return value must be compatible with expected return type.
            if (!canAssignTypeOfExpressionToTypeReference(actualReturnType, expectedReturnType)) {
                throw fail(new TypeMismatchException(actualReturnType.toString(), statement.getLocation(),
                    "return value", this.getCurrentMethodDeclaration().toString(),
                    expectedReturnType.getBasicTypeReference().getDeclaration().getName()));
            }
        }
        else {
            // Plain return is only allowed in void methods.
            if (!expectedReturnType.isVoid()) {
                throw fail(new SemanticException("Must return value from non-void method",
                    this.getCurrentMethodDeclaration().toString(), statement.getLocation()));
            }
        }
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Void context) {}

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Void context) {
        statement.getType().accept(this, context);

        // Variables must not be of type void or array of void.

        if (statement.getType().isVoid() || statement.getType().isDimensionalVoid()) {
            throw fail(new SemanticException("Local variable type must not be void or array of void",
                this.getCurrentMethodDeclaration().toString(), statement.getLocation()));
        }

        // TODO: Is the variable declaration known when evaluating the initial value expression?
        this.addVariableDeclarationToCurrentScope(statement);

        if (statement.getValue().isPresent()) {
            statement.getValue().get().accept(this, context);

            // Type of initial value must be compatible with variable declaration.
            if (!canAssignTypeOfExpressionToTypeReference(statement.getValue().get().getType(), statement.getType())) {
                throw fail(new TypeMismatchException(null, statement.getLocation(),
                    "attempted assignment of type " + statement.getValue().get().getType().toString()
                        + " to " + statement.getType().toString(),
                    this.getCurrentMethodDeclaration().toString()));
            }
        }
    }

    @Override
    protected void visit(Statement.Block block, Void context) {
        this.enterNewVariableDeclarationScope();

        block.getStatements().forEach(node -> node.accept(this, context));

        this.leaveCurrentVariableDeclarationScope();
    }

    @Override
    protected void visit(Expression.BinaryOperation expression, Void context) {
        expression.getLeft().accept(this, context);
        expression.getRight().accept(this, context);

        TypeOfExpression typeOfLeftOperand = expression.getLeft().getType();
        TypeOfExpression typeOfRightOperand = expression.getRight().getType();

        switch (expression.getOperationType()) {
            case MULTIPLICATION:
            case DIVISION:
            case MODULO:
            case ADDITION:
            case SUBTRACTION:
                // Operands for numeric operations must be integers.
                if (!typeOfLeftOperand.isInteger()) {
                    throw fail(new TypeMismatchException(typeOfLeftOperand.toString(), expression.getLocation(),
                        "numeric operation '" + expression.getOperationType().getOperatorSymbol() + "'",
                        this.getCurrentMethodDeclaration().toString(), "int"));
                }

                if (!typeOfRightOperand.isInteger()) {
                    throw fail(new TypeMismatchException(typeOfRightOperand.toString(), expression.getLocation(),
                        "numeric operation '" + expression.getOperationType().getOperatorSymbol() + "'",
                        this.getCurrentMethodDeclaration().toString(), "int"));
                }

                expression.getType().resolveToInteger();
                break;

            case LOGICAL_AND:
            case LOGICAL_OR:
                // Operands for logical operations must be boolean.
                if (!typeOfLeftOperand.isBoolean()) {
                    throw fail(new TypeMismatchException(typeOfLeftOperand.toString(), expression.getLocation(),
                        "logical operation '" + expression.getOperationType().getOperatorSymbol() + "'",
                        this.getCurrentMethodDeclaration().toString(), "boolean"));
                }
                if (!typeOfRightOperand.isBoolean()) {
                    throw fail(new TypeMismatchException(typeOfRightOperand.toString(), expression.getLocation(),
                        "logical operation '" + expression.getOperationType().getOperatorSymbol() + "'",
                        this.getCurrentMethodDeclaration().toString(), "boolean"));
                }

                expression.getType().resolveToBoolean();
                break;

            case LESS_THAN:
            case LESS_THAN_OR_EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL_TO:
                // Operands for numeric comparison must be integers.
                if (!typeOfLeftOperand.isInteger()) {
                    throw fail(new TypeMismatchException(typeOfLeftOperand.toString(), expression.getLocation(),
                        "comparison '" + expression.getOperationType().getOperatorSymbol() + "'",
                        this.getCurrentMethodDeclaration().toString(), "int"));
                }
                if (!typeOfRightOperand.isInteger()) {
                    throw fail(new TypeMismatchException(typeOfRightOperand.toString(), expression.getLocation(),
                        "comparison '" + expression.getOperationType().getOperatorSymbol() + "'",
                        this.getCurrentMethodDeclaration().toString(), "int"));
                }

                expression.getType().resolveToBoolean();
                break;

            case EQUAL_TO:
            case NOT_EQUAL_TO:
                // Left and right operands must be comparable.
                if (!canCheckForEqualityWithTypesOfExpressions(typeOfLeftOperand, typeOfRightOperand)) {
                    throw fail(new TypeMismatchException(null, expression.getLocation(),
                        "equality check between types " + typeOfLeftOperand.toString()
                            + " and " + typeOfRightOperand.toString(),
                        this.getCurrentMethodDeclaration().toString()));
                }

                expression.getType().resolveToBoolean();
                break;

            case ASSIGNMENT:
                // Left operand must be assignable.
                if (!typeOfLeftOperand.isAssignable()) {
                    throw fail(new SemanticException("Left side not assignable",
                        this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
                }

                // Left and right operands must be compatible.
                if (!canAssignTypeOfExpressionToTypeOfExpression(typeOfRightOperand, typeOfLeftOperand)) {
                    throw fail(new TypeMismatchException(null, expression.getLocation(),
                        "attempted assignment of type " + typeOfRightOperand.toString()
                            + " to " + typeOfLeftOperand.toString(),
                        this.getCurrentMethodDeclaration().toString()));
                }

                expression.getType().resolveToTypeOfExpression(typeOfLeftOperand, false);
                break;

            default:
                throw new AssertionError();
        }
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, Void context) {
        expression.getOther().accept(this, context);

        switch (expression.getOperationType()) {
            case LOGICAL_NEGATION:
                // Operand for logical negation must be boolean.
                if (!expression.getOther().getType().isBoolean()) {
                    throw fail(new TypeMismatchException(expression.getOther().getType().toString(),
                        expression.getLocation(), "logical negation",
                        this.getCurrentMethodDeclaration().toString(),
                        "boolean"));
                }

                expression.getType().resolveToBoolean();
                break;

            case NUMERIC_NEGATION:
                // Operand for numeric negation must be integer.
                if (!expression.getOther().getType().isInteger()) {
                    throw fail(new TypeMismatchException(expression.getOther().getType().toString(),
                        expression.getLocation(), "numerical negation", this.getCurrentMethodDeclaration().toString(),
                        "int"));
                }

                expression.getType().resolveToInteger();
                break;

            default:
                throw new AssertionError();
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

        // Literal value must fit in signed 32 bit.
        try {
            Integer.parseInt(expression.getValue());
        }
        catch (NumberFormatException exception) {
            throw fail(new SemanticException("Integer literal too big", this.getCurrentMethodDeclaration().toString(),
                expression.getLocation()));
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

            // Methods cannot be invoked on null literal.
            if (!typeOfContext.getDeclaration().isPresent()) {
                throw fail(new SemanticException("Cannot invoke method on null",
                    this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
            }

            // Methods cannot be invoked on arrays.
            if (typeOfContext.getNumberOfDimensions() != 0) {
                throw fail(new SemanticException("Cannot invoke method on array",
                    this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
            }

            BasicTypeDeclaration typeDeclaration = typeOfContext.getDeclaration().get();

            // Methods can only be invoked on objects.
            if (!(typeDeclaration instanceof ClassDeclaration)) {
                throw fail(new SemanticException("Cannot invoke method on non-object",
                    this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
            }

            methodDeclaration = this.getMethodDeclarationForName(methodName, (ClassDeclaration)typeDeclaration);
        }
        else {
            // Must not invoke methods in static context.
            if (this.getCurrentMethodDeclaration() instanceof MainMethodDeclaration) {
                throw fail(new SemanticException("Cannot access 'this' in static context",
                    this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
            }

            methodDeclaration = this.getMethodDeclarationForName(methodName, this.getCurrentClassDeclaration());
        }

        // Method reference must be resolvable.
        if (!methodDeclaration.isPresent()) {
            throw fail(new SemanticException("Use of undeclared method '" + methodName + "'",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        List<TypeOfExpression> typesOfArguments = expression.getArgumentTypes();
        List<TypeReference> typesOfParameters = methodDeclaration.get().getParameterTypes();

        // Number of arguments must match.
        if (typesOfArguments.size() != typesOfParameters.size()) {
            throw fail(new SemanticException("Received incorrect number of arguments"
                + " (" + typesOfArguments.size() + " instead of " + typesOfParameters.size() + ")"
                + " for call to method '" + methodDeclaration.get().getName() + "'",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        for (int index = 0; index < typesOfArguments.size(); index += 1) {

            // Type of argument must be compatible.
            if (!canAssignTypeOfExpressionToTypeReference(typesOfArguments.get(index), typesOfParameters.get(index))) {
                throw fail(new TypeMismatchException(typesOfArguments.get(index).toString(),
                    expression.getLocation(),
                    "method argument '" + methodDeclaration.get().getParameters().get(index).getName() + "'"
                    + " for method '" + methodDeclaration.get().getName() + "'",
                    this.getCurrentMethodDeclaration().toString(),
                    typesOfParameters.get(index).getBasicTypeReference().getDeclaration().getName()));
            }
        }

        expression.getMethodReference().resolveTo(methodDeclaration.get());
        expression.getType().resolveToTypeReference(methodDeclaration.get().getReturnType(), false);
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Void context) {

        expression.getContext().accept(this, context);

        TypeOfExpression typeOfContext = expression.getContext().getType();

        // Fields cannot be accessed on null literal.
        if (!typeOfContext.getDeclaration().isPresent()) {
            throw fail(new SemanticException("Cannot access field on null",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        // Fields cannot be accessed on arrays.
        if (typeOfContext.getNumberOfDimensions() != 0) {
            throw fail(new SemanticException("Cannot access field on array",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        BasicTypeDeclaration typeDeclaration = typeOfContext.getDeclaration().get();

        // Fields can only be accessed on objects.
        if (!(typeDeclaration instanceof ClassDeclaration)) {
            throw fail(new SemanticException("Cannot access field on non-object",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        String fieldName = expression.getFieldReference().getName();
        ClassDeclaration classDeclaration = (ClassDeclaration)typeDeclaration;
        Optional<FieldDeclaration> fieldDeclaration = this.getFieldDeclarationForName(fieldName, classDeclaration);

        // Field reference must be resolvable.
        if (!fieldDeclaration.isPresent()) {
            throw fail(new SemanticException("Use of undeclared field '" + fieldName + "'",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        expression.getFieldReference().resolveTo(fieldDeclaration.get());
        expression.getType().resolveToVariableDeclaration(fieldDeclaration.get());
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {
        expression.getContext().accept(this, context);
        expression.getIndex().accept(this, context);

        TypeOfExpression typeOfContext = expression.getContext().getType();

        // Context must not be null literal.
        if (!typeOfContext.getDeclaration().isPresent()) {
            throw fail(new SemanticException("Context of array access must not be null",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        // Context must be an array of some sort.
        if (typeOfContext.getNumberOfDimensions() < 1) {
            throw fail(new SemanticException("Context of array access must be array",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        // Array index must be an integer.
        if (!expression.getIndex().getType().isInteger()) {
            throw fail(new TypeMismatchException(expression.getIndex().getType().toString(), expression.getLocation(),
                "array index", this.getCurrentMethodDeclaration().toString(), "int"));
        }

        BasicTypeDeclaration basicTypeDeclaration = typeOfContext.getDeclaration().get();
        int numberOfDimensions = typeOfContext.getNumberOfDimensions() - 1;

        expression.getType().resolveToArrayOf(basicTypeDeclaration, numberOfDimensions, true);
    }

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {
        String name = expression.getVariableReference().getName();
        Optional<VariableDeclaration> variableDeclaration = this.getVariableDeclarationForName(name);

        if (!variableDeclaration.isPresent()) {
            // Check for classes that might shadow global declarations.
            // If we found a class, we can reject the program as it cannot have static fields or be
            // accessed as a reference.
            if (this.getClassDeclarationForName(name).isPresent()) {
                throw fail(new SemanticException("Cannot reference class with name '" + name + "' directly",
                    this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
            }

            // Retrieve global declarations
            variableDeclaration = this.getGlobalVariableDeclarationForName(name);
        }

        // Variable reference must be resolvable.
        if (!variableDeclaration.isPresent()) {
            throw fail(new SemanticException("Use of undeclared variable '" + name + "'",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        // Variable must be accessible. arguments parameter for main method is not accessible.
        if (!variableDeclaration.get().canBeAccessed()) {
            throw fail(new SemanticException("Variable '"
                + variableDeclaration.get().getName() + "' may not be accessed",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        // Check whether we are trying to access a field (which is always non-static since we do not support
        // static fields) from a static context.
        if (this.getCurrentMethodDeclaration() instanceof MainMethodDeclaration
            && variableDeclaration.get() instanceof FieldDeclaration) {
            throw fail(new SemanticException("Cannot access non-static field "
                + variableDeclaration.get().getName() + " from static context",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        expression.getVariableReference().resolveTo(variableDeclaration.get());
        expression.getType().resolveToVariableDeclaration(variableDeclaration.get());
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {

        // Current context may not be accessed in main methods.
        if (this.getCurrentMethodDeclaration() instanceof MainMethodDeclaration) {
            throw fail(new SemanticException("Cannot access 'this' in static context",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        expression.getType().resolveToInstanceOfClass(this.getCurrentClassDeclaration(), false);
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        String className = expression.getClassReference().getName();
        Optional<ClassDeclaration> classDeclaration = this.getClassDeclarationForName(className);

        // Class reference must ve resolvable.
        if (!classDeclaration.isPresent()) {
            throw fail(new SemanticException("Use of undeclared class '" + className + "'",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        expression.getClassReference().resolveTo(classDeclaration.get());
        expression.getType().resolveToInstanceOfClass(classDeclaration.get(), false);
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        expression.getPrimaryDimension().accept(this, context);

        // Primary dimension must be integer.
        if (!expression.getPrimaryDimension().getType().isInteger()) {
            throw fail(new TypeMismatchException(expression.getPrimaryDimension().getType().toString(),
                expression.getLocation(), "primary array dimension",
                this.getCurrentMethodDeclaration().toString(), "int"));
        }

        String name = expression.getBasicTypeReference().getName();
        Optional<BasicTypeDeclaration> basicTypeDeclaration = this.getBasicTypeDeclarationForName(name);

        // Basic type reference must be resolvable.
        if (!basicTypeDeclaration.isPresent()) {
            throw fail(new SemanticException("Use of undeclared class '" + name + "'",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        // Must not be array of void.
        if (basicTypeDeclaration.get() == PrimitiveTypeDeclaration.VOID) {
            throw fail(new SemanticException("Array of void is not allowed",
                this.getCurrentMethodDeclaration().toString(), expression.getLocation()));
        }

        expression.getBasicTypeReference().resolveTo(basicTypeDeclaration.get());
        expression.getType().resolveToArrayOf(basicTypeDeclaration.get(),
            expression.getNumberOfDimensions(), false);
    }
}
