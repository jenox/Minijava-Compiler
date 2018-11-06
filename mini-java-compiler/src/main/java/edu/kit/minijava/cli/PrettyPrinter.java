package edu.kit.minijava.cli;

import edu.kit.minijava.ast2.nodes.*;
import edu.kit.minijava.ast2.references.*;

import java.util.*;

public class PrettyPrinter implements ASTVisitor {

//    private int expressionStatementDepth;
//    private boolean nested;
//    private static final String INDENT = "\t";

    public PrettyPrinter() {
//        depth = 0;
//        expressionStatementDepth = 0;
//        nested = false;
    }

    // MARK: - Classes

    public String format(Program program) {
        this.builder.setLength(0);

        this.visit(program);

        return this.builder.toString();
    }

    public void visit(Program program) {
        List<ClassDeclaration> declarations = new ArrayList<>(program.classes);
        declarations.sort(Comparator.comparing(ClassDeclaration::getName));

        for (ClassDeclaration declaration : declarations) {
            declaration.accept(this);
        }
    }

    public void visit(ClassDeclaration declaration) {
        this.print("class " + declaration.getName());
        this.beginBlock();

        List<MethodDeclaration> methods = new ArrayList<>(declaration.getMethodDeclarations());
        methods.sort(Comparator.comparing(MethodDeclaration::getName));

        List<FieldDeclaration> fields = new ArrayList<>(declaration.getFieldDeclarations());
        fields.sort(Comparator.comparing(FieldDeclaration::getName));

        for (MethodDeclaration method : methods) {
            method.accept(this);
        }

        for (FieldDeclaration field : fields) {
            field.accept(this);
        }

        this.endBlock();
    }

    public void visit(FieldDeclaration declaration) {
        this.print("public ");
        this.print(declaration.getType());
        this.print(" ");
        this.print(declaration.getName());
        this.println(";");
    }

    public void visit(MethodDeclaration declaration) {
        this.print("public ");

        if (declaration.isStatic()) {
            this.print("static ");
        }

        this.print(declaration.getReturnType());
        this.print(" ");
        this.print(declaration.getName());
        this.print("(");

        if (!declaration.getParameterTypes().isEmpty()) {
            String separator = "";

            for (ParameterDeclaration parameter : declaration.getParameters()) {
                this.print(separator);
                parameter.accept(this);

                separator = ", ";
            }
        }

        this.print(")");
        this.beginBlock();
        this.endBlock();
    }

    public void visit(ParameterDeclaration declaration) {
        this.print(declaration.getType());
        this.print(" ");
        this.print(declaration.getName());
    }

    public void visit(Statement.IfStatement statement) {}
    public void visit(Statement.WhileStatement statement) {}
    public void visit(Statement.ExpressionStatement statement) {}
    public void visit(Statement.ReturnStatement statement) {}
    public void visit(Statement.EmptyStatement statement) {}
    public void visit(Statement.Block statement) {}
    public void visit(Statement.LocalVariableDeclarationStatement statement) {}

    public void visit(Expression.BinaryOperation expression) {}
    public void visit(Expression.UnaryOperation expression) {}
    public void visit(Expression.NullLiteral expression) {}
    public void visit(Expression.BooleanLiteral expression) {}
    public void visit(Expression.IntegerLiteral expression) {}
    public void visit(Expression.MethodInvocation expression) {}
    public void visit(Expression.ExplicitFieldAccess expression) {}
    public void visit(Expression.ArrayElementAccess expression) {}
    public void visit(Expression.VariableAccess expression) {}
    public void visit(Expression.CurrentContextAccess expression) {}
    public void visit(Expression.NewObjectCreation expression) {}
    public void visit(Expression.NewArrayCreation expression) {}

    /*
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
        if (!nested) {
            printLeftBrace();
            if (block.statements.isEmpty()) {
                print(" }\n"); // whitespace, right brace, newline
                printWhitespace();
                return;
            }
            depth++;
            newLine();
            printWhitespace();
        }
        if (block.statements.isEmpty()) {
            print(" ");
            return;
        }
        BlockStatement firstStatement = block.statements.get(0);
        boolean currentNested = nested;
        nested = true;
        int start = 0;
        if (true) {
            firstStatement.accept(this);
            start = 1;
        }
        for (int i = start; i < block.statements.size(); i++) {
            BlockStatement statement = block.statements.get(i);
            newLine();
            printWhitespace();
            statement.accept(this);
        }
        nested = currentNested;
        if (!nested) {
            depth--; // back to top level
            newLine();
            printWhitespace();
            printRightBrace();
        }

    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        print(String.valueOf(booleanLiteral.value));
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
        printLeftPar();
        ifElseStatement.condition.accept(this);
        printRightPar();
        print(" {\n");
        depth++;
        boolean localNested = nested;
        nested = true;
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
        nested = localNested;
        newLine();
        printWhitespace();
        printRightBrace();
        newLine();
    }

    @Override
    public void visit(IfStatement ifStatement) {
        print("if ");
        printLeftPar();
        ifStatement.condition.accept(this);
        printRightPar();
        print(" {\n");
        depth++;
        boolean localNested = nested;
        nested = true;
        printWhitespace();
        ifStatement.statementIfTrue.accept(this);
        newLine();
        depth--;
        nested = localNested;
        printWhitespace();
        printRightBrace();
        newLine();
    }

    @Override
    public void visit(IntegerLiteral integerLiteral) {
        print(integerLiteral.value);
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
    public void visit(PostfixExpression postfixExpression) {
        postfixExpression.expression.accept(this);
        postfixExpression.postfixOperation.accept(this);
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
    public void visit(WhileStatement whileStatement) {
        print("while ");
        printLeftPar();
        whileStatement.condition.accept(this);
        printRightPar();
        print(" {\n");
        depth++;
        boolean localNested = nested;
        nested = true;
        printWhitespace();
        whileStatement.statementWhileTrue.accept(this);
        newLine();
        depth--;
        nested = localNested;
        printWhitespace();
        printRightBrace();
        newLine();
    }

    */

    // MARK: - Output

    private final StringBuilder builder = new StringBuilder();

    private int indentationDepth = 0;
    private boolean hasPrintedIndentationForCurrentLine = false;

    private void print(String string) {
        if (!this.hasPrintedIndentationForCurrentLine) {
            for (int index = 0; index < this.indentationDepth; index += 1) {
                this.builder.append("\t");
            }

            this.hasPrintedIndentationForCurrentLine = true;
        }

        this.builder.append(string);
    }

    private void println(String string) {
        this.print(string);
        this.printNewline();
    }

    private void printNewline() {
        this.builder.append("\n");
        this.hasPrintedIndentationForCurrentLine = false;
    }

    private void beginBlock() {
        this.print(" {");
        this.indentationDepth += 1;
        this.printNewline();
    }

    private void endBlock() {
        this.indentationDepth -= 1;
        this.print("}");
        this.printNewline();
    }

    private void print(TypeReference reference) {
        this.print(reference.name);

        for (int index = 0; index < reference.numberOfDimensions; index += 1) {
            this.print("[]");
        }
    }

    /**
     * prints depth number of times tab without linebreak at end
     */
//    private void printWhitespace() {
//        for (int i = 0; i < depth; i++) {
//            this.builder.append(INDENT);
//        }
//    }

//    private void printLeftPar() {
//        this.builder.append("(");
//    }
//
//    private void printRightPar() {
//        this.builder.append(")");
//    }
//
//    private void printLeftBrace() {
//        this.builder.append("{");
//    }
//
//    private void printRightBrace() {
//        this.builder.append("}");
//    }
//
//    private void newLine() {
//        this.builder.append("\n");
//    }
}
