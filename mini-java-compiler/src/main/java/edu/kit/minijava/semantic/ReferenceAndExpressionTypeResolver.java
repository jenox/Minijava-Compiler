package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;

public class ReferenceAndExpressionTypeResolver extends ASTVisitor<ClassDeclaration> {
    public ReferenceAndExpressionTypeResolver(Program program, ClassAndMemberNameConflictChecker checker) {
        this.checker = checker;
        this.symbolTable = new SymbolTable();

        program.accept(this);
    }

    private final ClassAndMemberNameConflictChecker checker;
    private final SymbolTable symbolTable;

    private boolean hasCollectedDeclarationsForUseBeforeDeclare = false;


    // MARK: - Traversal

    @Override
    protected void visit(Program program, ClassDeclaration context) {

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
    protected void visit(ClassDeclaration classDeclaration, ClassDeclaration context) {
        assert this.symbolTable.getNumberOfScopes() == 0;

        this.symbolTable.enterNewScope();

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            fieldDeclaration.accept(this, classDeclaration);
        }

        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {
            methodDeclaration.accept(this, classDeclaration);
        }

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(FieldDeclaration fieldDeclaration, ClassDeclaration context) {
        if (!this.hasCollectedDeclarationsForUseBeforeDeclare) {
            this.resolve(fieldDeclaration.getType());
            return;
        }

        this.symbolTable.enterDeclaration(fieldDeclaration);
    }

    @Override
    protected void visit(MethodDeclaration methodDeclaration, ClassDeclaration context) {
        if (!this.hasCollectedDeclarationsForUseBeforeDeclare) {
            this.resolve(methodDeclaration.getReturnType());
            return;
        }

        this.symbolTable.enterNewScope();

        for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
            parameterDeclaration.accept(this, context);
        }

        methodDeclaration.getBody().accept(this, context);

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(ParameterDeclaration parameterDeclaration, ClassDeclaration context) {
        this.resolve(parameterDeclaration.getType());

        this.symbolTable.enterDeclaration(parameterDeclaration);
    }

    @Override
    protected void visit(Statement.IfStatement statement, ClassDeclaration context) {
        statement.getCondition().accept(this, context);
        statement.getStatementIfTrue().accept(this, context);
        statement.getStatementIfFalse().ifPresent(node -> node.accept(this, context));
    }

    @Override
    protected void visit(Statement.WhileStatement statement, ClassDeclaration context) {
        statement.getCondition().accept(this, context);
        statement.getStatementWhileTrue().accept(this, context);
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, ClassDeclaration context) {
        statement.getExpression().accept(this, context);
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, ClassDeclaration context) {
        statement.getValue().ifPresent(node -> node.accept(this, context));
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, ClassDeclaration context) {}

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, ClassDeclaration context) {
        statement.getValue().ifPresent(node -> node.accept(this));

        this.resolve(statement.getType());

        assert !this.symbolTable.hasVariableDeclarationInCurrentScopeWithName(statement.getName()) :
        "variable already defined in current scope";

        Optional<VariableDeclaration> declaration = this.symbolTable.getVisibleDeclarationForName(statement.getName());

        if (declaration.isPresent()) {
            assert declaration.get().canBeShadowedByVariableDeclarationInNestedScope() :
            declaration.get() + " cannot be shadowed by " + statement;
        }

        this.symbolTable.enterDeclaration(statement);
    }

    @Override
    protected void visit(Statement.Block block, ClassDeclaration context) {
        this.symbolTable.enterNewScope();

        for (Statement statement : block.getStatements()) {
            statement.accept(this, context);
        }

        this.symbolTable.leaveCurrentScope();
    }

    @Override
    protected void visit(Expression.BinaryOperation expression, ClassDeclaration context) {
        expression.getLeft().accept(this, context);
        expression.getRight().accept(this, context);
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, ClassDeclaration context) {
        expression.getOther().accept(this, context);
    }

    @Override
    protected void visit(Expression.NullLiteral expression, ClassDeclaration context) {
        expression.getType().resolveToNull();
    }

    @Override
    protected void visit(Expression.BooleanLiteral expression, ClassDeclaration context) {
        expression.getType().resolveTo(PrimitiveTypeDeclaration.BOOLEAN);
    }

    @Override
    protected void visit(Expression.IntegerLiteral expression, ClassDeclaration context) {
        expression.getType().resolveTo(PrimitiveTypeDeclaration.INTEGER);
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, ClassDeclaration context) {
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

            methodDeclaration = this.checker.getInstanceMethodDeclaration(methodName, classDeclaration);
        }
        else {
            assert this.checker.getInstanceMethodDeclaration(methodName, context) != null : "use of undeclared method";

            methodDeclaration = this.checker.getInstanceMethodDeclaration(methodName, context);
        }

        expression.getReference().resolveTo(methodDeclaration);
        expression.getType().resolveTo(methodDeclaration.getReturnType());

        System.out.println("method access " + expression.getReference());
        System.out.println();
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, ClassDeclaration context) {
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
        expression.getType().resolveTo(fieldDeclaration.getType());
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, ClassDeclaration context) {
        expression.getContext().accept(this, context);
        expression.getIndex().accept(this, context);

        TypeOfExpression type = expression.getContext().getType();
        assert type.getDeclaration().isPresent() : "context of array access must be array";
        assert type.getNumberOfDimensions() >= 1 : "context of array access must be array";

        expression.getType().resolveTo(type.getDeclaration().get(), type.getNumberOfDimensions() - 1);
    }

    @Override
    protected void visit(Expression.VariableAccess expression, ClassDeclaration context) {
        String name = expression.getReference().getName();
        Optional<VariableDeclaration> declaration = this.symbolTable.getVisibleDeclarationForName(name);

        assert declaration.isPresent() : "use of undeclared variable " + name;

        expression.getReference().resolveTo(declaration.get());

        expression.getType().resolveTo(declaration.get().getType());
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, ClassDeclaration context) {
        expression.getType().resolveTo(context);
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, ClassDeclaration context) {
        this.resolve(expression.getReference());

        expression.getType().resolveTo(expression.getReference());
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, ClassDeclaration context) {
        expression.getPrimaryDimension().accept(this, context);

        TypeOfExpression primaryDimensionType = expression.getPrimaryDimension().getType();
        assert primaryDimensionType.getDeclaration().isPresent() : "dim must be int";
        assert primaryDimensionType.getDeclaration().get() == PrimitiveTypeDeclaration.INTEGER : "dim must be int";
        assert primaryDimensionType.getNumberOfDimensions() == 0 : "dim must be int";

        this.resolve(expression.getReference());

        expression.getType().resolveTo(expression.getReference(), expression.getNumberOfDimensions());
    }


    // MARK: - Helpers

    private Optional<BasicTypeDeclaration> basicTypeNamed(String name) {
        switch (name) {
            case "void": return Optional.of(PrimitiveTypeDeclaration.VOID);
            case "int": return Optional.of(PrimitiveTypeDeclaration.INTEGER);
            case "boolean": return Optional.of(PrimitiveTypeDeclaration.BOOLEAN);
            default: return Optional.ofNullable(this.classNamed(name).orElseGet(null));
        }
    }

    private Optional<ClassDeclaration> classNamed(String name) {
        return Optional.ofNullable(this.checker.getClassDeclaration(name));
    }

    private void resolve(TypeReference reference) {
        Optional<BasicTypeDeclaration> declaration = this.basicTypeNamed(reference.getName());

        assert declaration.isPresent() :
                "use of undeclared identifier " + reference.getName() + " at " + reference.getLocation();

        reference.resolveTo(declaration.get());
    }

    private void resolve(BasicTypeReference reference) {
        Optional<BasicTypeDeclaration> declaration = this.basicTypeNamed(reference.getName());

        assert declaration.isPresent() :
                "use of undeclared identifier " + reference.getName() + " at " + reference.getLocation();

        reference.resolveTo(declaration.get());
    }

    private void resolve(ClassReference reference) {
        Optional<ClassDeclaration> declaration = this.classNamed(reference.getName());

        assert declaration.isPresent() :
                "use of undeclared identifier " + reference.getName() + " at " + reference.getLocation();

        reference.resolveTo(declaration.get());
    }
}
