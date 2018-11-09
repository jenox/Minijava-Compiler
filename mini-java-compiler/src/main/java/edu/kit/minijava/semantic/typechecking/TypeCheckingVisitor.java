package edu.kit.minijava.semantic.typechecking;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Statement.*;
import edu.kit.minijava.ast.references.*;
import edu.kit.minijava.ast.references.TypeOfExpression.Type;
import edu.kit.minijava.lexer.TokenLocation;

/**
 *
 * Visits nodes of AST to check and set types.
 *
 * TypeContext is used for passing type information to nodes in the following way
 *
 * - statements pass context to child nodes, such that afterwards contexts contains type of child nodes
 *
 * - statemtents check type of child nodes
 *
 * - expressions set type of given context according to own type
 *
 * - expressions check type of subexpressions
 *
 */
public class TypeCheckingVisitor implements ASTVisitor<TypeContext> {

    public TypeCheckingVisitor() {

    }

    @Override
    public void visit(Program program, TypeContext context) {
        for (ClassDeclaration decl : program.getClassDeclarations()) {
            decl.accept(this, context);
        }

    }

    @Override
    public void visit(ClassDeclaration classDeclaration, TypeContext context) {
        // visit mehthods
        for (MethodDeclaration decl : classDeclaration.getMethodDeclarations()) {
            decl.accept(this, context);
        }

    }

    @Override
    public void visit(FieldDeclaration fieldDeclaration, TypeContext context) {
        // set type of context
        context.setType(fieldDeclaration.getType());
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, TypeContext context) {
        // visit method body
        methodDeclaration.accept(this, context);

    }

    @Override
    public void visit(ParameterDeclaration parameterDeclaration, TypeContext context) {
        // set type of context
        context.setType(parameterDeclaration.getType());
    }

    @Override
    public void visit(IfStatement statement, TypeContext context) {
        // check type of condition
        TypeContext condition = new TypeContext();
        statement.getCondition().accept(this, condition);

        if (!condition.isBoolean()) {
            this.markError("Condition in if statement not boolean");
        }

        // visit statement if true
        statement.getStatementIfTrue().accept(this, context);

        // visit statement if false
        Statement falseStatement = statement.getStatementIfFalse();
        if (falseStatement != null) {
            falseStatement.accept(this, context);
        }
    }

    @Override
    public void visit(WhileStatement statement, TypeContext context) {
        // check type of condition
        TypeContext condition = new TypeContext();
        statement.getCondition().accept(this, condition);

        if (!condition.isBoolean()) {
            this.markError("Condition in while not boolean");
        }

        // visit statement
        statement.getStatementWhileTrue().accept(this, context);
    }

    @Override
    public void visit(ExpressionStatement statement, TypeContext context) {
        statement.getExpression().accept(this, context);
    }

    @Override
    public void visit(ReturnStatement statement, TypeContext context) {
        Expression exp = statement.getValue();
        TypeContext childContext = new TypeContext();
        if (context.isVoid()) {
            if (exp != null) {
                // method should not have return value
                this.markError("method should not return a value");
            }
        }
        else {
            // check that return type equals specified type
            if (exp == null) {
                // missing return value
                this.markError("missing return value");
            }
            else {
                exp.accept(this, childContext);

                if (!context.isCompatible(childContext)) {
                    this.markError("Types do not match");
                }
            }
        }

    }

    @Override
    public void visit(EmptyStatement statement, TypeContext context) {
        // nothing to do
    }

    @Override
    public void visit(Block statement, TypeContext context) {
        // visit statements
        for (Statement s : statement.getStatements()) {
            s.accept(this, context);
        }
    }

    @Override
    public void visit(LocalVariableDeclarationStatement statement, TypeContext context) {
        context.setType(statement.getType());
    }

    @Override
    public void visit(BinaryOperation expression, TypeContext context) {
        boolean isAssignment = false;
        switch (expression.getOperationType()) {
            case ADDITION:
            case MULTIPLICATION:
            case DIVISION:
            case MODULO:
            case SUBTRACTION:
                expression.getType().resolveTo(Type.INT);
                context.setArithmetic();
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
            default: throw new IllegalStateException("unknown binary operator");
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
                this.markError("Types do not match");
            }

        }
        else {
            // check that children have needed type
            TypeContext leftContext = new TypeContext();
            TypeContext rightContext = new TypeContext();

            expression.getLeft().accept(this, leftContext);
            expression.getRight().accept(this, rightContext);

            if (expression.getType().getType() == Type.BOOLEAN
                    && (!leftContext.isBoolean() || !rightContext.isBoolean())) {
                this.markError("Wrong expression type");
            }

            else if (expression.getType().getType() == Type.INT
                    && (!leftContext.isArithmetic() || !rightContext.isArithmetic())) {
                this.markError("Wrong expression type");
            }
            else {
                throw new IllegalStateException("unknown type");
            }
        }

    }

    @Override
    public void visit(UnaryOperation expression, TypeContext context) {
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
            this.markError("Incorrect type");
        }

        expression.accept(this, context);

        if (!compare.isCompatible(context)) {
            this.markError("Types do not match");
        }

    }

    @Override
    public void visit(NullLiteral expression, TypeContext context) {
        context.setNull();
        expression.getType().resolveTo(Type.NULL);
    }

    @Override
    public void visit(BooleanLiteral expression, TypeContext context) {
        context.setBoolean();
        expression.getType().resolveTo(Type.BOOLEAN);
    }

    @Override
    public void visit(IntegerLiteral expression, TypeContext context) {
        context.setArithmetic();
        String intValue = expression.getValue();
        // in order to check if intValue is a valid int we just try to get
        // convert the string to an integer
        try {
            Integer.parseInt(intValue);
        }
        catch (NumberFormatException e) {
            this.markError("Not an int");
        }

        expression.getType().resolveTo(Type.INT);
    }

    @Override
    public void visit(MethodInvocation expression, TypeContext context) {
        // set return type
        MethodReference method = expression.getReference();
        TypeReference returnType = method.getDeclaration().getReturnType();
        context.setType(returnType); //set type of this expression to return type
        this.setExpressionType(expression, context);

        // check parameters
        int numberOfPassedParams = method.getArgumentTypes().size();
        int numberOfDeclaredParams = method.getDeclaration().getParameters().size();

        if (numberOfDeclaredParams != numberOfPassedParams) {
            this.markError("Wrong number of parameters");
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
                this.markError("Invalid parameter type");
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
    public void visit(ExplicitFieldAccess expression, TypeContext context) {
        //get type of expression
        TypeContext childContext = new TypeContext();
        expression.getContext().accept(this, childContext);
        //set type of expression
        this.setExpressionType(expression, childContext);

        //set parent context to type of accessed field
        TypeReference ref = expression.getReference().getDeclaration().getType();
        context.setType(ref);

    }

    @Override
    public void visit(ArrayElementAccess expression, TypeContext context) {
        //get Type of expresion
        TypeContext childContext = new TypeContext();
        expression.getContext().accept(this, childContext);
        //check that type is array type
        if (childContext.getReference() != Reference.TYPE || childContext.getNumberOfDimensions() == 0) {
            this.markError("not an error");
        }

        //set parent context to type of array element
        //type of array element has one dimension less than array itself
        context.setType(childContext.getTypeRef());
        context.reduceDimension();

    }

    @Override
    public void visit(VariableAccess expression, TypeContext context) {
        //set type of expression
        TypeReference varRef = expression.getReference().getDeclaration().getType();
        TypeContext varContext = new TypeContext(varRef);
        this.setExpressionType(expression, varContext);

        //set parent context
        context.setType(varRef);

    }

    @Override
    public void visit(CurrentContextAccess expression, TypeContext context) {
        //nothing to do
    }

    @Override
    public void visit(NewObjectCreation expression, TypeContext context) {
        //set type of expression
        String name = expression.getReference().getName();
        TokenLocation location = expression.getReference().getLocation();
        TypeReference typeReference = new TypeReference(name, 0, location);
        context.setType(typeReference);
        this.setExpressionType(expression, context);
    }

    @Override
    public void visit(NewArrayCreation expression, TypeContext context) {
        //set type of expression
        String name = expression.getReference().getName();
        TokenLocation tokenLocation = expression.getReference().getLocation();
        int numOfDims = expression.getNumberOfDimensions();

        TypeReference ref = new TypeReference(name, numOfDims, tokenLocation);
        context.setType(ref);
        this.setExpressionType(expression, context);
    }

    private void setExpressionType(Expression exp, TypeContext context) {
        switch (context.getReference()) {
            case ARTIHMETIC:
                exp.getType().resolveTo(Type.INT);
                break;
            case BOOLEAN:
                exp.getType().resolveTo(Type.BOOLEAN);
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

    private void markError(String s) {
        // throw new TypeCheckingException();
        // for debugging purposes
        System.out.println(s);
    }
}
