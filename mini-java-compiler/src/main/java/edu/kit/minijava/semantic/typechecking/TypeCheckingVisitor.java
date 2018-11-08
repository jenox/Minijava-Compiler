package edu.kit.minijava.semantic.typechecking;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.nodes.Expression.*;
import edu.kit.minijava.ast.nodes.Statement.*;
import edu.kit.minijava.ast.references.TypeReference;

public class TypeCheckingVisitor implements ASTVisitor<TypeContext> {

    private static final String INT_NAME = "int";
    private static final String BOOLEAN_NAME = "boolean";

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
        this.setContextByRef(fieldDeclaration.getType(), context);
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, TypeContext context) {
        // visit method body
        methodDeclaration.accept(this, context);

    }

    @Override
    public void visit(ParameterDeclaration parameterDeclaration, TypeContext context) {
        // set type of context
        this.setContextByRef(parameterDeclaration.getType(), context);
    }

    @Override
    public void visit(IfStatement statement, TypeContext context) {
        // check type of condition
        statement.getCondition().accept(this, TypeContext.BOOLEAN);

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
        statement.getCondition().accept(this, TypeContext.BOOLEAN);

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
        if (this.isVoid(context)) {
            if (exp != null) {
                // method should not have return value
                //throw new TypeCheckException();
            }
        }
        else {
            // check that return type equals specified type
            if (exp == null) {
                // missing return value
                //throw new TypeCheckException();
            }
            else {
                exp.accept(this, context);
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
        this.setContextByRef(statement.getType(), context);
    }

    @Override
    public void visit(BinaryOperation expression, TypeContext context) {
        TypeContext childContext; // which type should children have
        TypeContext thisContext = null; // type of expression
        boolean isAssignment = false;
        switch (expression.getOperationType()) {
            case ADDITION:
            case MULTIPLICATION:
            case DIVISION:
            case MODULO:
            case SUBTRACTION:
                thisContext = TypeContext.ARITHMETIC;
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL_TO:
            case EQUAL_TO:
            case NOT_EQUAL_TO:
                childContext = TypeContext.ARITHMETIC;
                break;
            case LOGICAL_AND:
            case LOGICAL_OR:
                childContext = TypeContext.BOOLEAN;
                thisContext = TypeContext.BOOLEAN;
                break;
            case ASSIGNMENT:
                isAssignment = true;
            default:
                childContext = null;
        }
        if (isAssignment) {
            // get Type of left expression, which is a variable or a field
            TypeContext leftContext = new TypeContext(Reference.UNKNOWN);
            expression.getLeft().accept(this, leftContext);
            // compare with type of right value by giving context to child node
            expression.getRight().accept(this, leftContext);
            // type of expression is type of assigned value, needed for assignemnts within binary operations, like
            // if ((i = next()) < 42)

            // check that leftContext, ie. type of expression is same as type specified by context
            if (!leftContext.equals(context)) {
                //  throw new TypeCheckException();
            }
        }
        else {
            assert thisContext != null;

            // make sure this expression has same type as specified in given context
            if (!thisContext.equals(context)) {
                //  throw new TypeCheckException();
            }

            // check that children have needed type
            expression.getLeft().accept(this, childContext);
            expression.getRight().accept(this, childContext);
        }

    }

    @Override
    public void visit(UnaryOperation expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NullLiteral expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BooleanLiteral expression, TypeContext context) {
        context.setBoolean();
    }

    @Override
    public void visit(IntegerLiteral expression, TypeContext context) {
        context.setArithmetic();
        //TODO: check that expression is valid integer
    }

    @Override
    public void visit(MethodInvocation expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ExplicitFieldAccess expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ArrayElementAccess expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(VariableAccess expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CurrentContextAccess expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NewObjectCreation expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NewArrayCreation expression, TypeContext context) {
        // TODO Auto-generated method stub

    }

    private boolean isVoid(TypeContext context) {
        return TypeContext.VOID.equals(context);
    }

    private void setContextByRef(TypeReference ref, TypeContext context) {
        switch (ref.getName()) {
            case INT_NAME:
                context.setArithmetic();
                break;
            case BOOLEAN_NAME:
                context.setArithmetic();
                break;
            default:
                context.setType(ref);
        }
    }
}
