package edu.kit.minijava.cli;

import java.util.*;

import edu.kit.minijava.ast.*;

public class PrettyPrinter implements ASTVisitor {

    private int depth;
    private int expressionStatementDepth;
    
    private static final String INDENT = "\t";

    public PrettyPrinter() {
        depth = 0;
        expressionStatementDepth = 0;
    }

    public String format(Program program) {
        this.builder.setLength(0);

        this.visit(program);
      
        return this.builder.toString();
    }

    @Override
    public void visit(AddExpression addExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        addExpression.left.accept(this);
        print(" + ");
        addExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        print("[");
        arrayAccess.index.accept(this);
        print("]");
    }

    @Override
    public void visit(AssignmentExpression assignmentExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        assignmentExpression.left.accept(this);
        print(" = ");
        assignmentExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
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
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        divideExpression.left.accept(this);
        print(" / ");
        divideExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
            printRightPar();
        }
    }

    @Override
    public void visit(EmptyStatement emptyStatement) {
        print(";");
    }

    @Override
    public void visit(EqualToExpression equalToExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        equalToExpression.left.accept(this);
        print(" == ");
        equalToExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        expressionStatement.expression.accept(this);
        print(";");
    }

    @Override
    public void visit(Field field) {
        print("public ");
        field.type.accept(this);
        print(" "); //whitespace
        print(field.name);
        print(";");
        newLine();
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        print("." + fieldAccess.fieldName);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        greaterThanExpression.left.accept(this);
        print(" > ");
        greaterThanExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(GreaterThanOrEqualToExpression greaterThanOrEqualToExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        greaterThanOrEqualToExpression.left.accept(this);
        print(" >= ");
        greaterThanOrEqualToExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(IdentifierAndArgumentsExpression identifierAndArgumentsExpression) {
        print(identifierAndArgumentsExpression.identifier);
        printLeftPar();
        String separator = "";
        for (Expression exp : identifierAndArgumentsExpression.arguments) {
            // Separate argument list with commas after the first arguments
            print(separator);
            separator = ", ";

            exp.accept(this);
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
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        lessThanExpression.left.accept(this);
        print(" < ");
        lessThanExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(LessThanOrEqualToExpression lessThanOrEqualToExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        lessThanOrEqualToExpression.left.accept(this);
        print(" <= ");
        lessThanOrEqualToExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(LocalVariableDeclarationStatement localVariableDeclarationStatement) {
        localVariableDeclarationStatement.type.accept(this);
        print(" " + localVariableDeclarationStatement.name + ";");
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
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        logicalAndExpression.left.accept(this);
        print(" && ");
        logicalAndExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(LogicalNotExpression logicalNotExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        print("!");
        logicalNotExpression.other.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(LogicalOrExpression logicalOrExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        logicalOrExpression.left.accept(this);
        print(" || ");
        logicalOrExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(MainMethod mainMethod) {
        print("public static void " + mainMethod.name + "(String[] " + mainMethod.argumentsParameterName + ") ");
        mainMethod.body.accept(this);
        newLine();
    }

    @Override
    public void visit(Method method) {
        print("public ");
        method.returnType.accept(this);
        print(" ");
        print(method.name + "(");
        if (!method.parameters.isEmpty()) {
            String separator = "";
            for (int i = 0; i < method.parameters.size(); i++) {
                this.print(separator);
                separator = ", ";
                method.parameters.get(i).accept(this);
            }
        }
        print(") ");
        method.body.accept(this);
        newLine();
    }

    @Override
    public void visit(MethodInvocation methodInvocation) {
        print(".");
        print(methodInvocation.methodName + "(");
        if (!methodInvocation.arguments.isEmpty()) {
            String separator = "";
            for (Expression expression : methodInvocation.arguments) {
                print(separator);
                separator = ", ";
                expression.accept(this);
            }
        }
        print(")");
    }

    @Override
    public void visit(ModuloExpression moduloExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        moduloExpression.left.accept(this);
        print(" % ");
        moduloExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(MultiplyExpression multiplyExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        multiplyExpression.left.accept(this);
        print(" * ");
        multiplyExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(NegateExpression negateExpression) {
        if (expressionStatementDepth > 0) {
            printLeftPar();
        }
        expressionStatementDepth++;
        print("-");
        negateExpression.other.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
    }

    @Override
    public void visit(NewArrayExpression newArrayExpression) {
        print("new ");
        newArrayExpression.type.accept(this);
        print(" [");
        newArrayExpression.primaryDimension.accept(this);
        print("]");
        for (int i = 1; i < newArrayExpression.numberOfDimensions; i++) {
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
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        notEqualToExpression.left.accept(this);
        print(" != ");
        notEqualToExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
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

        newLine();
    }

    @Override
    public void visit(ReturnNoValueStatement returnNoValueStatement) {
        print("return;");
    }

    @Override
    public void visit(ReturnValueStatement returnValueStatement) {
        print("return ");
        returnValueStatement.returnValue.accept(this);
        print(";");
    }

    @Override
    public void visit(SubtractExpression subtractExpression) {
        if (expressionStatementDepth > 0) {
        printLeftPar();
        }
        expressionStatementDepth++;
        subtractExpression.left.accept(this);
        print(" - ");
        subtractExpression.right.accept(this);
        expressionStatementDepth--;
        if (expressionStatementDepth > 0) {
        printRightPar();
        }
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



    // MARK: - Output

    private final StringBuilder builder = new StringBuilder();

    /**
     * prints depth number of times tab without linebreak at end
     */
    private void printWhitespace() {
        for (int i = 0; i < depth; i++) {
            this.builder.append(INDENT);
        }
    }

    private void print(String s) {
        this.builder.append(s);
    }

    private void printLeftPar() {
        this.builder.append("(");
    }

    private void printRightPar() {
        this.builder.append(")");
    }

    private void printLeftBrace() {
        this.builder.append("{");
    }

    private void printRightBrace() {
        this.builder.append("}");
    }

    private void newLine() {
        this.builder.append("\n");
    }
}
