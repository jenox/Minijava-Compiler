package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Statement.*;
import edu.kit.minijava.ast.references.MethodReference;
import edu.kit.minijava.ast.references.TypeOfExpression;
import edu.kit.minijava.ast.references.TypeOfExpression.Type;
import edu.kit.minijava.ast.references.TypeReference;
import edu.kit.minijava.lexer.TokenLocation;

import java.util.Optional;

/**
 * Visits nodes of AST to set declarations, check and set types.
 * <p>
 * TypeContext is used for passing type information to nodes in the following way
 * <p>
 * - statements pass context to child nodes, such that afterwards contexts contains type of child nodes
 * - statements check type of child nodes
 * - expressions set type of given context according to own type
 * - expressions check type of subexpressions
 */
public class DeclarationAndTypeChecker implements ASTVisitor<TypeContext, SemanticAnalysisException> {

    private SymbolTable<VariableDeclaration> variableSymbolTable = new SymbolTable<>();
    private ClassDeclaration currentClass;
    private Program astRoot = null;


    public void resolveDeclarationsAndTypes(Program program) throws SemanticAnalysisException {
        this.astRoot = program;

        this.visit(program, null);
    }

    @Override
    public void visit(Program program, TypeContext context) throws SemanticAnalysisException {
        for (ClassDeclaration decl : program.getClassDeclarations()) {
            decl.accept(this, context);
        }

    }

    @Override
    public void visit(ClassDeclaration classDeclaration, TypeContext context) throws SemanticAnalysisException {
        this.variableSymbolTable.enterNewScope();

        // TODO Encapsulate the current class context more elegantly in a context var
        this.currentClass = classDeclaration;
        this.variableSymbolTable.enterAllDeclarations(classDeclaration.getFieldSymbolTable());

        // Visit each method
        for (MethodDeclaration decl : classDeclaration.getMethodDeclarations()) {
            decl.accept(this, context);
        }

        this.variableSymbolTable.leaveCurrentScope();
    }

    @Override
    public void visit(FieldDeclaration fieldDeclaration, TypeContext context) throws SemanticAnalysisException {
        // Set type of context
        context.setType(fieldDeclaration.getType());

        // Field has already been set in class's symbol table when visiting class for the first time
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, TypeContext context) throws SemanticAnalysisException {

        // Method itself has already been set in class's symbol table when visiting class for the first time

        // Resolve parameter declarations and add to symbol table
        this.variableSymbolTable.enterNewScope();

        // Visit method parameters and set declarations
        for (ParameterDeclaration declaration : methodDeclaration.getParameters()) {
            declaration.accept(this, context);
        }

        // Visit method body
        TypeContext returnContext = new TypeContext();
        returnContext.setType(methodDeclaration.getReturnType());

        methodDeclaration.getBody().accept(this, returnContext);

        this.variableSymbolTable.leaveCurrentScope();
    }

    @Override
    public void visit(ParameterDeclaration parameterDeclaration, TypeContext context) throws SemanticAnalysisException {

        this.variableSymbolTable.enterDeclaration(parameterDeclaration.getName(), parameterDeclaration);

        // Set type of context
        if (context != null) {
            context.setType(parameterDeclaration.getType());
        }
    }

    @Override
    public void visit(IfStatement statement, TypeContext context) throws SemanticAnalysisException {
        // Check type of condition
        TypeContext condition = new TypeContext();

        this.variableSymbolTable.enterNewScope();
        statement.getCondition().accept(this, condition);
        this.variableSymbolTable.leaveCurrentScope();

        if (!condition.isBoolean()) {
            throw new TypeMismatchException("Condition in if statement not boolean");
        }

        // Visit statement if true
        this.variableSymbolTable.enterNewScope();
        statement.getStatementIfTrue().accept(this, context);
        this.variableSymbolTable.leaveCurrentScope();

        // Visit statement if false
        Statement falseStatement = statement.getStatementIfFalse();
        if (falseStatement != null) {
            this.variableSymbolTable.enterNewScope();
            falseStatement.accept(this, context);
            this.variableSymbolTable.leaveCurrentScope();
        }
    }

    @Override
    public void visit(WhileStatement statement, TypeContext context) throws SemanticAnalysisException {
        // check type of condition
        TypeContext condition = new TypeContext();

        this.variableSymbolTable.enterNewScope();
        statement.getCondition().accept(this, condition);
        this.variableSymbolTable.leaveCurrentScope();

        if (!condition.isBoolean()) {
            throw new TypeMismatchException("Condition in while not boolean");
        }

        // Visit statement
        this.variableSymbolTable.enterNewScope();
        statement.getStatementWhileTrue().accept(this, context);
        this.variableSymbolTable.leaveCurrentScope();
    }

    @Override
    public void visit(ExpressionStatement statement, TypeContext context) throws SemanticAnalysisException {
        TypeContext expression = new TypeContext();
        statement.getExpression().accept(this, expression);
    }

    @Override
    public void visit(ReturnStatement statement, TypeContext context) throws SemanticAnalysisException {
        Expression exp = statement.getValue();
        TypeContext childContext = new TypeContext();
        if (context.isVoid()) {
            if (exp != null) {
                // method should not have return value
                throw new TypeMismatchException("Method should not return a value");
            }
        }
        else {
            // check that return type equals specified type
            if (exp == null) {
                // missing return value
                throw new TypeMismatchException("Missing return value");
            }
            else {
                exp.accept(this, childContext);

                if (!context.isCompatible(childContext)) {
                    throw new TypeMismatchException("Types do not match");
                }
            }
        }

    }

    @Override
    public void visit(EmptyStatement statement, TypeContext context) throws SemanticAnalysisException {
        // Nothing to do here
    }

    @Override
    public void visit(Block statement, TypeContext context) throws SemanticAnalysisException {
        // Enter new scope and visit statements

        this.variableSymbolTable.enterNewScope();

        for (Statement s : statement.getStatements()) {
            TypeContext statementContext = new TypeContext(context);
            s.accept(this, statementContext);
        }

        this.variableSymbolTable.leaveCurrentScope();
    }

    @Override
    public void visit(LocalVariableDeclarationStatement statement, TypeContext context)
        throws SemanticAnalysisException {

        this.variableSymbolTable.enterDeclaration(statement.getName(), statement);

        if (context != null) {
            context.setType(statement.getType());
        }
    }

    @Override
    public void visit(BinaryOperation expression, TypeContext context) throws SemanticAnalysisException {
        boolean isAssignment = false;
        switch (expression.getOperationType()) {
            case ADDITION:
            case MULTIPLICATION:
            case DIVISION:
            case MODULO:
            case SUBTRACTION:
                expression.getType().resolveTo(Type.INT);
                context.setArithmetic();
                break;
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL_TO:
            case EQUAL_TO:
            case NOT_EQUAL_TO:
                expression.getType().resolveTo(Type.BOOLEAN);
                context.setBoolean();
                break;
            case LOGICAL_AND:
            case LOGICAL_OR:
                expression.getType().resolveTo(Type.BOOLEAN);
                context.setBoolean();
                break;
            case ASSIGNMENT:
                isAssignment = true;
                break;
            default:
                throw new IllegalStateException("unknown binary operator");
        }
        if (isAssignment) {
            // get Type of left expression, which is a variable or a field
            // pass context as argument, so it will be set to type of assigned variable
            expression.getLeft().accept(this, context);
            // get Type of right expression
            TypeContext rightContext = new TypeContext(Reference.UNKNOWN);
            expression.getRight().accept(this, rightContext);
            // type of expression is type of assigned value, needed for assignemnts within binary operations, like
            // if ((i = next()) < 42)

            // check that leftContext and rightContext are compatible
            if (!context.isCompatible(rightContext)) {
                throw new TypeMismatchException("Types do not match");
            }

        }
        else {
            // check that children have needed type
            TypeContext leftContext = new TypeContext();
            TypeContext rightContext = new TypeContext();

            expression.getLeft().accept(this, leftContext);
            expression.getRight().accept(this, rightContext);

            if (expression.getType().getType() == Type.BOOLEAN) {
                switch (expression.getOperationType()) {
                    case LESS_THAN:
                    case LESS_THAN_OR_EQUAL_TO:
                    case GREATER_THAN:
                    case GREATER_THAN_OR_EQUAL_TO:
                        if (!leftContext.isArithmetic() || !rightContext.isArithmetic()) {
                            throw new TypeMismatchException(
                                "Wrong expression type: Comparison operators require arithmetic operands.");
                        }
                        break;
                    case EQUAL_TO:
                    case NOT_EQUAL_TO:
                        if (leftContext.isArithmetic() && rightContext.isArithmetic()) break;
                        if (leftContext.isBoolean() && rightContext.isBoolean()) break;

                        // We fell through, types are not compatible
                        throw new TypeMismatchException("Wrong expression type");
                    case LOGICAL_AND:
                    case LOGICAL_OR:
                        if (!leftContext.isBoolean() || !rightContext.isBoolean()) {
                            throw new TypeMismatchException(
                                "Wrong expression type: Logical operators require boolean operands");
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unknown boolean operator");
                }
            }
            else if (expression.getType().getType() == Type.INT
                && (!leftContext.isArithmetic() || !rightContext.isArithmetic())) {
                throw new TypeMismatchException("Wrong expression type");
            }
        }

    }

    @Override
    public void visit(UnaryOperation expression, TypeContext context) throws SemanticAnalysisException {
        TypeContext compare = null;
        boolean correctType = false;
        switch (expression.getOperationType()) {
            case LOGICAL_NEGATION:
                compare = new TypeContext(Reference.BOOLEAN);
                correctType = context.isBoolean();
                expression.getType().resolveTo(Type.BOOLEAN);
                break;
            case NUMERIC_NEGATION:
                compare = new TypeContext(Reference.ARTIHMETIC);
                correctType = context.isArithmetic();
                expression.getType().resolveTo(Type.INT);
                break;
            default:
                throw new IllegalStateException("unknown unary operator");
        }

        if (!correctType) {
            throw new TypeMismatchException("Incorrect type");
        }

        expression.accept(this, context);

        if (!compare.isCompatible(context)) {
            throw new TypeMismatchException("Types do not match");
        }

    }

    @Override
    public void visit(NullLiteral expression, TypeContext context) throws SemanticAnalysisException {
        context.setNull();
        expression.getType().resolveTo(Type.NULL);
    }

    @Override
    public void visit(BooleanLiteral expression, TypeContext context) throws SemanticAnalysisException {
        context.setBoolean();
        expression.getType().resolveTo(Type.BOOLEAN);
    }

    @Override
    public void visit(IntegerLiteral expression, TypeContext context) throws SemanticAnalysisException {
        context.setArithmetic();
        String intValue = expression.getValue();
        // in order to check if intValue is a valid int we just try to get
        // convert the string to an integer
        try {
            Integer.parseInt(intValue);
        }
        catch (NumberFormatException e) {
            throw new TypeMismatchException("Not an int");
        }

        expression.getType().resolveTo(Type.INT);
    }

    @Override
    public void visit(MethodInvocation expression, TypeContext context) throws SemanticAnalysisException {

        // First, visit context of the invocation
        TypeOfExpression methodTargetType;

        Expression invocationContext = expression.getContext();
        if (invocationContext != null) {
            expression.getContext().accept(this, context);

            methodTargetType = invocationContext.getType();
        }
        else {
            TypeReference contextType = new TypeReference(
                this.currentClass.getName(), 0, null);
            contextType.resolveTo(this.currentClass);
            methodTargetType = new TypeOfExpression();
            methodTargetType.resolveTo(contextType);

            // Make canonical representation
            expression.setContext(new CurrentContextAccess());
        }

        // Resolve name of method in the correct context

        MethodReference method = expression.getReference();

        // Retrieve method declaration
        Type methodType = methodTargetType.getType();

        if (methodType != Type.TYPE_REF) {
            throw new IllegalStateException("Method invocation on non type-ref type");
        }

        TypeReference methodTypeReference = methodTargetType.getReference();
        ClassDeclaration classDeclaration = this.astRoot.getClassSymbolTable().get(methodTypeReference.getName());

        if (classDeclaration == null) {
            throw new IllegalStateException("Method invocation with invalid type");
        }

        MethodDeclaration methodDeclaration = classDeclaration.getMethodSymbolTable().get(method.getName());

        if (methodDeclaration == null) {
            throw new UndeclaredUsageException(method.getName(), null);
        }

        method.resolveTo(methodDeclaration);

        // Set return type
        TypeReference returnType = method.getDeclaration().getReturnType();
        context.setType(returnType); //set type of this expression to return type
        this.setExpressionType(expression, context);

        // check parameters
        int numberOfPassedParams = method.getArgumentTypes().size();
        int numberOfDeclaredParams = method.getDeclaration().getParameters().size();

        if (numberOfDeclaredParams != numberOfPassedParams) {
            throw new TypeMismatchException("Wrong number of parameters");
        }

        // compare parameter types
        for (int i = 0; i < numberOfDeclaredParams; i++) {
            // get parameter type
            TypeReference type = method.getDeclaration().getParameterTypes().get(i);
            TypeContext typeContext = new TypeContext(type);

            // get type of expression
            Expression param = expression.getArguments().get(i);
            TypeContext paramContext = new TypeContext();
            param.accept(this, paramContext);

            // check expression type against parameter type
            if (!paramContext.isCompatible(typeContext)) {
                throw new TypeMismatchException("Invalid parameter type");
            }
            //set type of parameter expression
            else {
                this.setExpressionType(param, paramContext);
            }

            //visit body and pass return type as context
            Expression body = expression.getContext();
            if (body != null) {
                body.accept(this, context);
            }

        }

    }

    @Override
    public void visit(ExplicitFieldAccess expression, TypeContext context) throws SemanticAnalysisException {
        //get type of expression
        TypeContext childContext = new TypeContext();
        expression.getContext().accept(this, childContext);
        //set type of expression
        this.setExpressionType(expression, childContext);

        // Resolve declaration
        TypeOfExpression contextType = expression.getContext().getType();

        if (contextType.getType() != Type.TYPE_REF) {
            throw new IllegalStateException("Field access on non type-ref type");
        }

        TypeReference contextTypeRef = contextType.getReference();
        ClassDeclaration classDeclaration = this.astRoot.getClassSymbolTable().get(contextTypeRef.getName());

        if (classDeclaration == null) {
            throw new IllegalStateException("Field access with invalid type");
        }

        FieldDeclaration fieldDeclaration = classDeclaration.getFieldSymbolTable().get(contextTypeRef.getName());

        if (fieldDeclaration == null) {
            throw new UndeclaredUsageException(contextTypeRef.getName(), null);
        }

        expression.getReference().resolveTo(fieldDeclaration);

        //set parent context to type of accessed field
        TypeReference ref = expression.getReference().getDeclaration().getType();
        context.setType(ref);

    }

    @Override
    public void visit(ArrayElementAccess expression, TypeContext context) throws SemanticAnalysisException {
        // Get Type of expression
        TypeContext childContext = new TypeContext();
        expression.getContext().accept(this, childContext);

        // Check that type is array type
        if (childContext.getReference() != Reference.TYPE || childContext.getNumberOfDimensions() == 0) {
            throw new TypeMismatchException("Cannot access elements of non-array type.");
        }

        // Set parent context to type of array element
        // Type of array element has one dimension less than array itself
        context.setType(childContext.getTypeRef());
        context.reduceDimension();

    }

    @Override
    public void visit(VariableAccess expression, TypeContext context) throws SemanticAnalysisException {

        // Resolve name
        Optional<VariableDeclaration> declaration
            = this.variableSymbolTable.getVisibleDeclarationForIdentifier(expression.getReference().getName());

        if (!declaration.isPresent()) {
            throw new UndeclaredUsageException(expression.getReference().getName(),
                expression.getReference().getLocation());
        }

        expression.getReference().resolveTo(declaration.get());

        //set type of expression
        TypeReference varRef = expression.getReference().getDeclaration().getType();
        TypeContext varContext = new TypeContext(varRef, varRef.getNumberOfDimensions());
        this.setExpressionType(expression, varContext);

        //set parent context
        if (context != null) {
            context.setType(varRef);
        }

    }

    @Override
    public void visit(CurrentContextAccess expression, TypeContext context) throws SemanticAnalysisException {

    }

    @Override
    public void visit(NewObjectCreation expression, TypeContext context) throws SemanticAnalysisException {
        // Set type of expression
        String name = expression.getReference().getName();
        TokenLocation location = expression.getReference().getLocation();
        TypeReference typeReference = new TypeReference(name, 0, location);
        context.setType(typeReference);
        this.setExpressionType(expression, context);
    }

    @Override
    public void visit(NewArrayCreation expression, TypeContext context) throws SemanticAnalysisException {
        // Set type of expression
        String name = expression.getReference().getName();
        TokenLocation tokenLocation = expression.getReference().getLocation();
        int numOfDims = expression.getNumberOfDimensions();

        TypeReference ref = new TypeReference(name, numOfDims, tokenLocation);
        context.setType(ref);
        this.setExpressionType(expression, context);
    }

    private void setExpressionType(Expression exp, TypeContext context) throws SemanticAnalysisException {
        switch (context.getReference()) {
            case ARTIHMETIC:
                exp.getType().resolveTo(Type.INT);
                break;
            case BOOLEAN:
                exp.getType().resolveTo(Type.BOOLEAN);
                break;
            case NULL:
                exp.getType().resolveTo(Type.NULL);
                break;
            case TYPE:
                exp.getType().resolveTo(context.getTypeRef());
                break;
            default:
                throw new IllegalStateException("invalid expression type");
        }
    }
}
