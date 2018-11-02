package edu.kit.minijava.cli;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.kit.minijava.parser.AddExpression;
import edu.kit.minijava.parser.ArrayAccess;
import edu.kit.minijava.parser.AssignmentExpression;
import edu.kit.minijava.parser.Block;
import edu.kit.minijava.parser.BlockStatement;
import edu.kit.minijava.parser.BooleanLiteral;
import edu.kit.minijava.parser.BooleanType;
import edu.kit.minijava.parser.ClassDeclaration;
import edu.kit.minijava.parser.ClassMember;
import edu.kit.minijava.parser.DivideExpression;
import edu.kit.minijava.parser.EmptyStatement;
import edu.kit.minijava.parser.EqualToExpression;
import edu.kit.minijava.parser.Expression;
import edu.kit.minijava.parser.ExpressionStatement;
import edu.kit.minijava.parser.Field;
import edu.kit.minijava.parser.FieldAccess;
import edu.kit.minijava.parser.GreaterThanExpression;
import edu.kit.minijava.parser.GreaterThanOrEqualToExpression;
import edu.kit.minijava.parser.IdentifierAndArgumentsExpression;
import edu.kit.minijava.parser.IdentifierExpression;
import edu.kit.minijava.parser.IfElseStatement;
import edu.kit.minijava.parser.IfStatement;
import edu.kit.minijava.parser.IntegerLiteral;
import edu.kit.minijava.parser.IntegerType;
import edu.kit.minijava.parser.LessThanExpression;
import edu.kit.minijava.parser.LessThanOrEqualToExpression;
import edu.kit.minijava.parser.LocalVariableDeclarationStatement;
import edu.kit.minijava.parser.LocalVariableInitializationStatement;
import edu.kit.minijava.parser.LogicalAndExpression;
import edu.kit.minijava.parser.LogicalNotExpression;
import edu.kit.minijava.parser.LogicalOrExpression;
import edu.kit.minijava.parser.MainMethod;
import edu.kit.minijava.parser.Method;
import edu.kit.minijava.parser.MethodInvocation;
import edu.kit.minijava.parser.ModuloExpression;
import edu.kit.minijava.parser.MultiplyExpression;
import edu.kit.minijava.parser.NegateExpression;
import edu.kit.minijava.parser.NewArrayExpression;
import edu.kit.minijava.parser.NewObjectExpression;
import edu.kit.minijava.parser.NotEqualToExpression;
import edu.kit.minijava.parser.NullLiteral;
import edu.kit.minijava.parser.Parameter;
import edu.kit.minijava.parser.PostfixExpression;
import edu.kit.minijava.parser.PostfixOperation;
import edu.kit.minijava.parser.Program;
import edu.kit.minijava.parser.PropagatedException;
import edu.kit.minijava.parser.ReturnNoValueStatement;
import edu.kit.minijava.parser.ReturnValueStatement;
import edu.kit.minijava.parser.SubtractExpression;
import edu.kit.minijava.parser.ThisExpression;
import edu.kit.minijava.parser.Type;
import edu.kit.minijava.parser.UserDefinedType;
import edu.kit.minijava.parser.VoidType;
import edu.kit.minijava.parser.WhileStatement;
import util.INodeVisitor;

public class ParserPrinter implements INodeVisitor {

    private int depth;
    private Program program;

    public final static String INDENT = "\t";

    public ParserPrinter(Program program) {
        this.program = program;
        depth = 0;
    }

    public void print() {
        visit(program);
    }

    @Override
    public void visit(AddExpression addExpression) {
        printWhitespace();
        System.out.println("AddExpression");
        depth++;
        addExpression.left.accept(this);
        addExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        printWhitespace();
        System.out.println("ArrayAccess");
        depth++;
        arrayAccess.index.accept(this);
        depth--;
    }

    @Override
    public void visit(AssignmentExpression assignmentExpression) {
        printWhitespace();
        System.out.println("AssignmentExpression");
        depth++;
        assignmentExpression.left.accept(this);
        assignmentExpression.right.accept(this);
        depth--;

    }

    @Override
    public void visit(Block block) {
        printWhitespace();
        System.out.println("Block");
        depth++;
        for (BlockStatement statement : block.statements) {
            statement.accept(this);
        }
        depth--;
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        printWhitespace();
        System.out.println("BooleanLiteral (" + booleanLiteral.value + ")");
    }

    @Override
    public void visit(BooleanType booleanType) {
        printWhitespace();
        System.out.println("BooleanType");
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        printWhitespace();
        System.out.println("ClassDeclaration (" + classDeclaration.className + ")");
        depth++;
        List<ClassMember> members = new ArrayList<>(classDeclaration.members.size());
        for (ClassMember member : classDeclaration.members) {
            members.add(member);
        }
        // sort members
        members.sort(new Comparator<ClassMember>() {

            @Override
            public int compare(ClassMember o1, ClassMember o2) {
                if (o1.isMethod()) {
                    if (o2.isMethod()) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
                    } else {
                        // o1 is method, o2 is not
                        return 1;
                    }
                } else {
                    if (o2.isMethod()) {
                        // o2 is method, o1 is not
                        return -1;
                    } else {
                        // both are not a method
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
                    }
                }
            }
        });

        for (ClassMember member : members) {
            member.accept(this);
        }
        depth--;

    }

    @Override
    public void visit(DivideExpression divideExpression) {
        printWhitespace();
        System.out.println("DivideExpression");
        depth++;
        divideExpression.left.accept(this);
        divideExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(EmptyStatement emptyStatement) {
        printWhitespace();
        System.out.println("EmptyStatement");
    }

    @Override
    public void visit(EqualToExpression equalToExpression) {
        printWhitespace();
        System.out.println("EqualToExpression");
        depth++;
        equalToExpression.left.accept(this);
        equalToExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        printWhitespace();
        System.out.println("ExpressionStatement");
        depth++;
        expressionStatement.expression.accept(this);
        depth--;
    }

    @Override
    public void visit(Field field) {
        printWhitespace();
        System.out.println("Field (" + field.name + ")");
        depth++;
        field.type.accept(this);
        depth--;

    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        printWhitespace();
        System.out.println("FieldAccess (" + fieldAccess.fieldName + ")");
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        printWhitespace();
        System.out.println("GreaterThanExpression");
        depth++;
        greaterThanExpression.left.accept(this);
        greaterThanExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(GreaterThanOrEqualToExpression greaterThanOrEqualToExpression) {
        printWhitespace();
        System.out.println("GreaterThanOrEqualToExpression");
        depth++;
        greaterThanOrEqualToExpression.left.accept(this);
        greaterThanOrEqualToExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(IdentifierAndArgumentsExpression identifierAndArgumentsExpression) {
        printWhitespace();
        System.out.println("IdentifierAndArgumentsExpression (" + identifierAndArgumentsExpression.identifier + ")");
        depth++;
        for (Expression exp : identifierAndArgumentsExpression.arguments) {
            exp.accept(this);
        }
        depth--;
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        printWhitespace();
        System.out.println("IdentifierExpression (" + identifierExpression.identifier + ")");
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        printWhitespace();
        System.out.println("IfElseStatement");
        depth++;
        ifElseStatement.condition.accept(this);
        ifElseStatement.statementIfTrue.accept(this);
        ifElseStatement.statementIfFalse.accept(this);
        depth--;
    }

    @Override
    public void visit(IfStatement ifStatement) {
        printWhitespace();
        System.out.println("IfStatement");
        depth++;
        ifStatement.condition.accept(this);
        ifStatement.statementIfTrue.accept(this);
        depth--;
    }

    @Override
    public void visit(IntegerLiteral integerLiteral) {
        printWhitespace();
        System.out.println("IntegerLiteral (" + integerLiteral.value + ")");
    }

    @Override
    public void visit(IntegerType integerType) {
        printWhitespace();
        System.out.println("IntegerType");
    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        printWhitespace();
        System.out.println("LessThanExpression");
        depth++;
        lessThanExpression.left.accept(this);
        lessThanExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(LessThanOrEqualToExpression lessThanOrEqualToExpression) {
        printWhitespace();
        System.out.println("LessThanOrEqualToExpression");
        depth++;
        lessThanOrEqualToExpression.left.accept(this);
        lessThanOrEqualToExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(LocalVariableDeclarationStatement localVariableDeclarationStatement) {
        printWhitespace();
        System.out.println("LocalVariableDeclarationStatement (name: " + localVariableDeclarationStatement.name + ")");
        depth++;
        localVariableDeclarationStatement.type.accept(this);
        depth--;
    }

    @Override
    public void visit(LocalVariableInitializationStatement localVariableInitializationStatement) {
        printWhitespace();
        System.out.println(
                "LocalVariableInitializationStatement (name: " + localVariableInitializationStatement.name + ")");
        depth++;
        localVariableInitializationStatement.type.accept(this);
        localVariableInitializationStatement.value.accept(this);
        depth--;
    }

    @Override
    public void visit(LogicalAndExpression logicalAndExpression) {
        printWhitespace();
        System.out.println("LogicalAndExpression");
        depth++;
        logicalAndExpression.left.accept(this);
        logicalAndExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(LogicalNotExpression logicalNotExpression) {
        printWhitespace();
        System.out.println("LogicalNotExpression");
        depth++;
        logicalNotExpression.other.accept(this);
        depth--;
    }

    @Override
    public void visit(LogicalOrExpression logicalOrExpression) {
        printWhitespace();
        System.out.println("LogicalOrExpression");
        depth++;
        logicalOrExpression.left.accept(this);
        logicalOrExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(MainMethod mainMethod) {
        printWhitespace();
        System.out.println(
                "Main method (name: " + mainMethod.name + ", params: " + mainMethod.argumentsParameterName + ")");
        depth++;
        mainMethod.body.accept(this);
        depth--;
    }

    @Override
    public void visit(Method method) {
        printWhitespace();
        System.out.println("Method (" + method.name + ")");
        depth++;
        // print return type first
        method.returnType.accept(this);
        // print parameters
        for (Parameter param : method.parameters) {
            param.accept(this);
        }
        // print block
        method.body.accept(this);
        depth--;
    }

    @Override
    public void visit(MethodInvocation methodInvocation) {
        printWhitespace();
        System.out.println("MethodInvocation (" + methodInvocation.methodName + ")");
        depth++;
        for (Expression arg : methodInvocation.arguments) {
            arg.accept(this);
        }
        depth--;
    }

    @Override
    public void visit(ModuloExpression moduloExpression) {
        printWhitespace();
        System.out.println("ModuloExpression");
        depth++;
        moduloExpression.left.accept(this);
        moduloExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(MultiplyExpression multiplyExpression) {
        printWhitespace();
        System.out.println("MultiplyExpression");
        depth++;
        multiplyExpression.left.accept(this);
        multiplyExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(NegateExpression negateExpression) {
        printWhitespace();
        System.out.println("NegateExpression");
        depth++;
        negateExpression.other.accept(this);
        depth--;
    }

    @Override
    public void visit(NewArrayExpression newArrayExpression) {
        printWhitespace();
        System.out.println("NewArrayExpression (numOfDims: " + newArrayExpression.numberOfDimensions + ")");
        depth++;
        newArrayExpression.type.accept(this);
        newArrayExpression.primaryDimension.accept(this);
        depth--;
    }

    @Override
    public void visit(NewObjectExpression newObjectExpression) {
        printWhitespace();
        System.out.println("NewObjectExpression (" + newObjectExpression.className + ")");
    }

    @Override
    public void visit(NotEqualToExpression notEqualToExpression) {
        printWhitespace();
        System.out.println("NotEqualToEpression");
        depth++;
        notEqualToExpression.left.accept(this);
        notEqualToExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(NullLiteral nullLiteral) {
        printWhitespace();
        System.out.println("NullLiteral");
    }

    @Override
    public void visit(Parameter parameter) {
        printWhitespace();
        System.out.println("Parameter (" + parameter.name + ")");
        depth++;
        parameter.type.accept(this);
        depth--;
    }

    @Override
    public void visit(PostfixExpression postfixExpression) {
        printWhitespace();
        System.out.println("PostfixExpression");
        depth++;
        postfixExpression.expression.accept(this);
        postfixExpression.postfixOperation.accept(this);
        depth--;
    }

    @Override
    public void visit(Program program) {
        printWhitespace();
        System.out.println("Program");
        depth++;
        List<ClassDeclaration> classes = new ArrayList<>(program.classDeclarations.size());
        for (ClassDeclaration declaration : program.classDeclarations) {
            classes.add(declaration);
        }

        // sort classes by name
        classes.sort(new Comparator<ClassDeclaration>() {

            @Override
            public int compare(ClassDeclaration o1, ClassDeclaration o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.className, o2.className);
            }
        });

        for (ClassDeclaration declaration : classes) {
            declaration.accept(this);
        }
        depth--;

    }

    @Override
    public void visit(ReturnNoValueStatement returnNoValueStatement) {
        printWhitespace();
        System.out.println("ReturnNoValueStatement");
    }

    @Override
    public void visit(ReturnValueStatement returnValueStatement) {
        printWhitespace();
        System.out.println("ReturnValueStatement");
        depth++;
        returnValueStatement.returnValue.accept(this);
        depth--;
    }

    @Override
    public void visit(SubtractExpression subtractExpression) {
        printWhitespace();
        System.out.println("SubtractExpression");
        depth++;
        subtractExpression.left.accept(this);
        subtractExpression.right.accept(this);
        depth--;
    }

    @Override
    public void visit(ThisExpression thisExpression) {
        printWhitespace();
        System.out.println("ThisExpression");
    }

    @Override
    public void visit(Type type) {
        printWhitespace();
        System.out.println("Type (numOfDims: " + type.numberOfDimensions + ")");
        depth++;
        type.basicType.accept(this);
        depth--;
    }

    @Override
    public void visit(UserDefinedType userDefinedType) {
        printWhitespace();
        System.out.println("UserDefinedType(" + userDefinedType.name + ")");
    }

    @Override
    public void visit(VoidType voidType) {
        printWhitespace();
        System.out.println("VoidType");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        printWhitespace();
        System.out.println("WhileStatement");
        depth++;
        whileStatement.condition.accept(this);
        whileStatement.statementWhileTrue.accept(this);
    }

    /**
     * prints depth number of times tab without linebreak at end
     */
    private void printWhitespace() {
        for (int i = 0; i < depth; i++) {
            System.out.print(INDENT);
        }
    }

}
