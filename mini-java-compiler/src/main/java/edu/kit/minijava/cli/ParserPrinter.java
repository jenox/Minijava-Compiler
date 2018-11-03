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
import edu.kit.minijava.parser.NodeVisitor;
import edu.kit.minijava.parser.NotEqualToExpression;
import edu.kit.minijava.parser.NullLiteral;
import edu.kit.minijava.parser.Parameter;
import edu.kit.minijava.parser.PostfixExpression;
import edu.kit.minijava.parser.Program;
import edu.kit.minijava.parser.ReturnNoValueStatement;
import edu.kit.minijava.parser.ReturnValueStatement;
import edu.kit.minijava.parser.SubtractExpression;
import edu.kit.minijava.parser.ThisExpression;
import edu.kit.minijava.parser.Type;
import edu.kit.minijava.parser.UserDefinedType;
import edu.kit.minijava.parser.VoidType;
import edu.kit.minijava.parser.WhileStatement;

public class ParserPrinter implements NodeVisitor {

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
        printLeftPar();
        addExpression.left.accept(this);
        print(" + ");
        addExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        print("[");
        arrayAccess.index.accept(this);
        print("]");
    }

    @Override
    public void visit(AssignmentExpression assignmentExpression) {
        printLeftPar();
        assignmentExpression.left.accept(this);
        print(" = ");
        assignmentExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(Block block) {
        printLeftBrace();
        if (block.statements.isEmpty()) {
            print(" }\n"); // whitespace, right brace, newline
        } else {
            depth++; //indent statements
            for (BlockStatement statement : block.statements) {
                newLine();
                printWhitespace();
                statement.accept(this);
            }
            newLine();
            depth--; //back to top level
            printWhitespace();
            printRightBrace();
            newLine();
        }
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        print(String.valueOf(booleanLiteral.value));
    }

    @Override
    public void visit(BooleanType booleanType) {
        print("boolean");
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        printWhitespace();
        print("class " + classDeclaration.className + " {\n");
        depth++;
        //copy members to new list
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
            newLine();
            printWhitespace();
            member.accept(this);
        }
        depth--;
        printWhitespace();
        printRightBrace();
    }

    @Override
    public void visit(DivideExpression divideExpression) {
        printWhitespace();
        divideExpression.left.accept(this);
        print(" / ");
        divideExpression.right.accept(this);
    }

    @Override
    public void visit(EmptyStatement emptyStatement) {
        print(";");
    }

    @Override
    public void visit(EqualToExpression equalToExpression) {
        printLeftPar();
        equalToExpression.left.accept(this);
        print(" == ");
        equalToExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        expressionStatement.expression.accept(this);
    }

    @Override
    public void visit(Field field) {
        field.type.accept(this);
        print(" "); //whitespace
        print(field.name);
        print(";");
        newLine();
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        print(fieldAccess.fieldName + ".");
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        printLeftPar();
        greaterThanExpression.left.accept(this);
        print(" > ");
        greaterThanExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(GreaterThanOrEqualToExpression greaterThanOrEqualToExpression) {
        printLeftPar();
        greaterThanOrEqualToExpression.left.accept(this);
        print(" >= ");
        greaterThanOrEqualToExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(IdentifierAndArgumentsExpression identifierAndArgumentsExpression) {
        print(identifierAndArgumentsExpression.identifier);
        printLeftPar();
        for (Expression exp : identifierAndArgumentsExpression.arguments) {
            exp.accept(this);
            print(", "); //seperator
        }
        printRightPar();
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        print(identifierExpression.identifier);
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        print("if ");
        ifElseStatement.condition.accept(this);
        print(" {\n");
        depth++;
        printWhitespace();
        ifElseStatement.statementIfTrue.accept(this);
        depth--;
        newLine();
        printWhitespace();
        print("} else {\n");
        depth++;
        printWhitespace();
        ifElseStatement.statementIfFalse.accept(this);
        depth--;
        newLine();
        printWhitespace();
        printRightBrace();
        newLine();
    }

    @Override
    public void visit(IfStatement ifStatement) {
        print("if ");
        ifStatement.condition.accept(this);
        print(" {\n");
        depth++;
        printWhitespace();
        ifStatement.statementIfTrue.accept(this);
        newLine();
        depth--;
        printWhitespace();
        printRightBrace();
        newLine();
    }

    @Override
    public void visit(IntegerLiteral integerLiteral) {
        print(integerLiteral.value);
    }

    @Override
    public void visit(IntegerType integerType) {
        print("int");
    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        printLeftPar();
        lessThanExpression.left.accept(this);
        print(" < ");
        lessThanExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(LessThanOrEqualToExpression lessThanOrEqualToExpression) {
        printLeftPar();
        lessThanOrEqualToExpression.left.accept(this);
        print(" <= ");
        lessThanOrEqualToExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(LocalVariableDeclarationStatement localVariableDeclarationStatement) {
        localVariableDeclarationStatement.type.accept(this);
        print(" " + localVariableDeclarationStatement.name + ";\n");
    }

    @Override
    public void visit(LocalVariableInitializationStatement localVariableInitializationStatement) {
       localVariableInitializationStatement.type.accept(this);
       print(" " + localVariableInitializationStatement.name + " = ");
       localVariableInitializationStatement.value.accept(this);
       print(";");
    }

    @Override
    public void visit(LogicalAndExpression logicalAndExpression) {
        printLeftPar();
        logicalAndExpression.left.accept(this);
        print(" && ");
        logicalAndExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(LogicalNotExpression logicalNotExpression) {
        print("(!");
        logicalNotExpression.other.accept(this);
        printRightPar();
    }

    @Override
    public void visit(LogicalOrExpression logicalOrExpression) {
        printLeftPar();
        logicalOrExpression.left.accept(this);
        print(" || ");
        logicalOrExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(MainMethod mainMethod) {
        print("public static void " + mainMethod.name + "(String[] " + mainMethod.argumentsParameterName + ") ");
        mainMethod.body.accept(this);
        newLine();
    }

    @Override
    public void visit(Method method) {
        printWhitespace();
        print(method.name + "(");
        if (!method.parameters.isEmpty()) {
            method.parameters.get(0).accept(this);
            for (int i = 1; i < method.parameters.size(); i++) {
                print(", ");
                method.parameters.get(i).accept(this);
            }
        }
        print(") ");
        method.body.accept(this);
        newLine();
    }

    @Override
    public void visit(MethodInvocation methodInvocation) {
        printWhitespace();
        print(methodInvocation.methodName + "(");
        if (!methodInvocation.arguments.isEmpty()) {
            methodInvocation.arguments.get(0).accept(this);
            for (int i = 1; i < methodInvocation.arguments.size(); i++) {
                print(", ");
                methodInvocation.arguments.get(i).accept(this);
            }
        }
        print(")");
    }

    @Override
    public void visit(ModuloExpression moduloExpression) {
        printLeftPar();
        moduloExpression.left.accept(this);
        print(" % ");
        moduloExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(MultiplyExpression multiplyExpression) {
        printLeftPar();
        multiplyExpression.left.accept(this);
        print(" * ");
        multiplyExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(NegateExpression negateExpression) {
        print("(!");
        negateExpression.other.accept(this);
        printRightPar();
    }

    @Override
    public void visit(NewArrayExpression newArrayExpression) {
        print("new ");
        newArrayExpression.type.accept(this);
        print(" [");
        newArrayExpression.primaryDimension.accept(this);
        print("] ");
        for (int i = 0; i < newArrayExpression.numberOfDimensions; i++) {
            print("[]");
        }
    }

    @Override
    public void visit(NewObjectExpression newObjectExpression) {
        print("new " + newObjectExpression.className + "()");
        newObjectExpression.accept(this);
    }

    @Override
    public void visit(NotEqualToExpression notEqualToExpression) {
        printLeftPar();
        notEqualToExpression.left.accept(this);
        print(" != ");
        notEqualToExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(NullLiteral nullLiteral) {
        print("null");
    }

    @Override
    public void visit(Parameter parameter) {
        parameter.type.accept(this);
        print(" " + parameter.name);
    }

    @Override
    public void visit(PostfixExpression postfixExpression) {
        postfixExpression.expression.accept(this);
        postfixExpression.postfixOperation.accept(this);
    }

    @Override
    public void visit(Program program) {
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

    }

    @Override
    public void visit(ReturnNoValueStatement returnNoValueStatement) {
        print("return;");
    }

    @Override
    public void visit(ReturnValueStatement returnValueStatement) {
        print("(return");
        returnValueStatement.returnValue.accept(this);
        print(";)");
    }

    @Override
    public void visit(SubtractExpression subtractExpression) {
        printLeftPar();
        subtractExpression.left.accept(this);
        print(" - ");
        subtractExpression.right.accept(this);
        printRightPar();
    }

    @Override
    public void visit(ThisExpression thisExpression) {
        print("this");
    }

    @Override
    public void visit(Type type) {
        type.basicType.accept(this);
        for (int i = 0; i < type.numberOfDimensions; i++) {
            print("[]");
        }
    }

    @Override
    public void visit(UserDefinedType userDefinedType) {
        print(userDefinedType.name);
    }

    @Override
    public void visit(VoidType voidType) {
        print("void");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        print("while ");
        whileStatement.condition.accept(this);
        print(" {\n");
        depth++;
        printWhitespace();
        whileStatement.statementWhileTrue.accept(this);
        newLine();
        depth--;
        printWhitespace();
        printRightBrace();
        newLine();
    }

    /**
     * prints depth number of times tab without linebreak at end
     */
    private void printWhitespace() {
        for (int i = 0; i < depth; i++) {
            System.out.print(INDENT);
        }
    }

    private void print(String s) {
        System.out.print(s);
    }

    private void printLeftPar() {
        System.out.print("(");
    }

    private void printRightPar() {
        System.out.print(")");
    }

    private void printLeftBrace() {
        System.out.print("{");
    }

    private void printRightBrace() {
        System.out.print("}");
    }

    private void newLine() {
        System.out.println(); // creates new line
    }
}