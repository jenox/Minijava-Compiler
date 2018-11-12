package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;

public class ReferenceAndExpressionTypeResolver extends ASTVisitor<Void> {
    public ReferenceAndExpressionTypeResolver(Program program, ClassAndMemberNameConflictChecker checker) {
        this.checker = checker;

        program.accept(this, null);
    }

    private final ClassAndMemberNameConflictChecker checker;
    private final SymbolTable symbolTable = new SymbolTable();
    private final Stack<ClassDeclaration> classDeclarations = new Stack<>();
    private final Stack<SubroutineDeclaration> subroutineDeclarations = new Stack<>();

    private boolean hasCollectedDeclarationsForUseBeforeDeclare = false;


    // MARK: - Traversal

    @Override
    protected void visit(Program program, Void context) {

        // Pass 1: collect declarations

        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this, context);
        }

        // Pass 2: resolve remaining types and references
        this.hasCollectedDeclarationsForUseBeforeDeclare = true;

        for (ClassDeclaration classDeclaration : program.getClassDeclarations()) {
            classDeclaration.accept(this, context);
        }
    }

    @Override
    protected void visit(ClassDeclaration classDeclaration, Void context) {
        assert this.symbolTable.getNumberOfScopes() == 0;

        this.classDeclarations.push(classDeclaration);
        this.symbolTable.enterNewScope();

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this, context);
        }

        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {
            methodDeclaration.accept(this, context);
        }

        for (MainMethodDeclaration methodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            methodDeclaration.accept(this, context);
        }

        this.symbolTable.leaveCurrentScope();
        this.classDeclarations.pop();
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, Void context) {
        if (!this.hasCollectedDeclarationsForUseBeforeDeclare) {
            fieldDeclaration.getType().accept(this, context);
            // TODO: void or array of void not allowed here
            return;
        }

        this.symbolTable.enterDeclaration(fieldDeclaration);
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, Void context) {
        if (!this.hasCollectedDeclarationsForUseBeforeDeclare) {
            methodDeclaration.getReturnType().accept(this, context);
            // TODO: array of void not allowed here
            methodDeclaration.getParameters().forEach(node -> node.accept(this, context));
            return;
        }

        this.subroutineDeclarations.push(methodDeclaration);
        this.symbolTable.enterNewScope();

        methodDeclaration.getParameters().forEach(node -> node.accept(this, context));
        methodDeclaration.getBody().accept(this, context);

        this.symbolTable.leaveCurrentScope();
        this.subroutineDeclarations.pop();
    }

    @Override
    protected void visit(MainMethodDeclaration methodDeclaration, Void context) {
        if (!this.hasCollectedDeclarationsForUseBeforeDeclare) {
            methodDeclaration.getReturnType().accept(this, context);
            methodDeclaration.getArgumentsParameter().accept(this, context);
            return;
        }

        this.subroutineDeclarations.push(methodDeclaration);
        this.symbolTable.enterNewScope();

        methodDeclaration.getReturnType().accept(this, context);
        methodDeclaration.getArgumentsParameter().accept(this, context);
        methodDeclaration.getBody().accept(this, context);

        this.symbolTable.leaveCurrentScope();
        this.subroutineDeclarations.pop();
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, Void context) {
        if (!this.hasCollectedDeclarationsForUseBeforeDeclare) {
            parameterDeclaration.getType().accept(this, context);
            // TODO: void or array of void not allowed here
            return;
        }

        // Ensure existing declaration can be shadowed.
        this.symbolTable.getVisibleDeclarationForName(parameterDeclaration.getName()).ifPresent(previousDeclaration -> {
            assert !this.symbolTable.isDeclarationInCurrentScope(previousDeclaration ) :
                    "invalid parameter redeclaration";
            assert previousDeclaration.canBeShadowedByVariableDeclarationInNestedScope() :
                    "other declaration cant be shadowed";
        });

        this.symbolTable.enterDeclaration(parameterDeclaration);
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

        TypeReference expectedReturnType = this.subroutineDeclarations.peek().getReturnType();

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

        // Ensure existing declaration can be shadowed.
        this.symbolTable.getVisibleDeclarationForName(statement.getName()).ifPresent(previousDeclaration -> {
            assert !this.symbolTable.isDeclarationInCurrentScope(previousDeclaration ) :
                    "var already defined in current scope";
            assert previousDeclaration.canBeShadowedByVariableDeclarationInNestedScope() :
                    "other declaration cant be shadowed";
        });

        this.symbolTable.enterDeclaration(statement);

        // Ensure type of value matches (if present).
        if (statement.getValue().isPresent()) {
            statement.getValue().get().accept(this, context);

            assert statement.getValue().get().getType().isCompatibleWith(statement.getType()) :
                    "incompatible value for variable declaration";
        }
    }

    @Override
    protected void visit(Statement.Block block, Void context) {
        this.symbolTable.enterNewScope();

        for (Statement statement : block.getStatements()) {
            statement.accept(this, context);
        }

        this.symbolTable.leaveCurrentScope();
    }

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

        String methodName = expression.getReference().getName();
        MethodDeclaration methodDeclaration;

        if (expression.getContext().isPresent()) {
            TypeOfExpression typeOfContext = expression.getContext().get().getType();

            assert typeOfContext.getDeclaration().isPresent() : "cannot access method on expression of type null";
            assert typeOfContext.getNumberOfDimensions() == 0 : "cannot access method on array";

            BasicTypeDeclaration typeDeclaration = typeOfContext.getDeclaration().get();

            assert typeDeclaration instanceof ClassDeclaration : "can only access methods on objects";

            ClassDeclaration classDeclaration = (ClassDeclaration)typeDeclaration;

            assert this.checker.getInstanceMethodDeclaration(methodName, classDeclaration) != null :
                    "use of undeclared method";

            methodDeclaration = this.checker.getInstanceMethodDeclaration(methodName, classDeclaration);
        }
        else {
            ClassDeclaration classDeclaration = this.classDeclarations.peek();

            assert this.checker.getInstanceMethodDeclaration(methodName, classDeclaration) != null :
                    "use of undeclared method";

            methodDeclaration = this.checker.getInstanceMethodDeclaration(methodName, classDeclaration);
        }

        List<TypeOfExpression> argumentTypes = expression.getReference().getArgumentTypes();
        List<TypeReference> parameterTypes = methodDeclaration.getParameterTypes();

        assert argumentTypes.size() == parameterTypes.size() : "incorrect number of arguments";
        for (int index = 0; index < argumentTypes.size(); index += 1) {
            assert argumentTypes.get(index).isCompatibleWith(parameterTypes.get(index)) : "incompatible argument type";
        }

        expression.getReference().resolveTo(methodDeclaration);
        expression.getType().resolveTo(methodDeclaration.getReturnType(), false);
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
        String fieldName = expression.getReference().getName();

        assert this.checker.getFieldDeclaration(fieldName , classDeclaration) != null : "use of undeclared field";

        FieldDeclaration fieldDeclaration = this.checker.getFieldDeclaration(fieldName , classDeclaration);

        expression.getReference().resolveTo(fieldDeclaration);

        // TODO: Is `this.f().x = 42;` valid?
        expression.getType().resolveTo(fieldDeclaration.getType(), true);
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Void context) {
        expression.getContext().accept(this, context);
        expression.getIndex().accept(this, context);

        TypeOfExpression type = expression.getContext().getType();
        assert type.getDeclaration().isPresent() : "context of array access must be array";
        assert type.getNumberOfDimensions() >= 1 : "context of array access must be array";

        // TODO: Is `this.f()[23] = 42;` valid?
        expression.getType().resolveTo(type.getDeclaration().get(), type.getNumberOfDimensions() - 1, true);
    }

    @Override
    protected void visit(Expression.VariableAccess expression, Void context) {
        String name = expression.getReference().getName();
        Optional<VariableDeclaration> declaration = this.symbolTable.getVisibleDeclarationForName(name);

        assert declaration.isPresent() : "use of undeclared variable " + name;
        assert declaration.get().canBeAccessed() : "variable may not be accessed. sorry.";

        expression.getReference().resolveTo(declaration.get());
        expression.getType().resolveTo(declaration.get().getType(), true);
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Void context) {
        expression.getType().resolveTo(this.classDeclarations.peek(), false);
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Void context) {
        Optional<ClassDeclaration> classDeclaration = this.getClassNamed(expression.getReference().getName());
        assert classDeclaration.isPresent() : "use of undeclared class";

        expression.getReference().resolveTo(classDeclaration.get());
        expression.getType().resolveTo(classDeclaration.get(), false);
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Void context) {
        expression.getPrimaryDimension().accept(this, context);

        TypeOfExpression primaryDimensionType = expression.getPrimaryDimension().getType();
        assert primaryDimensionType.getDeclaration().isPresent() : "dim must be int";
        assert primaryDimensionType.getDeclaration().get() == PrimitiveTypeDeclaration.INTEGER : "dim must be int";
        assert primaryDimensionType.getNumberOfDimensions() == 0 : "dim must be int";

        Optional<BasicTypeDeclaration> typeDeclaration = this.getBasicTypeNamed(expression.getReference().getName());
        assert typeDeclaration.isPresent() : "use of undeclared type in new array";
        assert typeDeclaration.get() != PrimitiveTypeDeclaration.VOID : "array of void is not allowed";

        expression.getReference().resolveTo(typeDeclaration.get());
        expression.getType().resolveTo(typeDeclaration.get(), expression.getNumberOfDimensions(), false);
    }


    // MARK: - Helpers

    private Optional<BasicTypeDeclaration> getBasicTypeNamed(String name) {
        for (PrimitiveTypeDeclaration type : PrimitiveTypeDeclaration.values()) {
            if (name.equals(type.getName()) && type.canBeReferencedByUser()) {
                return Optional.of(type);
            }
        }

        return Optional.ofNullable(this.getClassNamed(name).orElse(null));
    }

    private Optional<ClassDeclaration> getClassNamed(String name) {
        return Optional.ofNullable(this.checker.getClassDeclaration(name));
    }
}
