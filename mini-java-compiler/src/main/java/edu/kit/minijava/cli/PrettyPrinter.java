package edu.kit.minijava.cli;

import edu.kit.minijava.ast2.nodes.*;
import edu.kit.minijava.ast2.references.*;

import java.util.*;

public class PrettyPrinter implements ASTVisitor<PrettyPrinter.Options> {

//    private int expressionStatementDepth;
//    private boolean nested;
//    private static final String INDENT = "\t";

    public PrettyPrinter() {
//        depth = 0;
//        expressionStatementDepth = 0;
//        nested = false;
    }

    public enum Options {
        DO_NOT_PRINT_NEWLINE_AFTER_BLOCK;
    }

    // MARK: - Classes

    public String format(Program program) {
        this.builder.setLength(0);

        this.visit(program, null);

        return this.builder.toString();
    }

    public void visit(Program program, Options context) {
        List<ClassDeclaration> declarations = new ArrayList<>(program.classes);
        declarations.sort(Comparator.comparing(ClassDeclaration::getName));

        for (ClassDeclaration declaration : declarations) {
            declaration.accept(this, null);
        }
    }

    public void visit(ClassDeclaration declaration, Options context) {
        this.print("class " + declaration.getName());
        this.beginBlock();

        List<MethodDeclaration> methods = new ArrayList<>(declaration.getMethodDeclarations());
        methods.sort(Comparator.comparing(MethodDeclaration::getName));

        List<FieldDeclaration> fields = new ArrayList<>(declaration.getFieldDeclarations());
        fields.sort(Comparator.comparing(FieldDeclaration::getName));

        for (MethodDeclaration method : methods) {
            method.accept(this, null);
        }

        for (FieldDeclaration field : fields) {
            field.accept(this, null);
        }

        this.endBlock(true);
    }

    public void visit(FieldDeclaration declaration, Options context) {
        this.print("public ");
        this.print(declaration.getType());
        this.print(" ");
        this.print(declaration.getName());
        this.println(";");
    }

    public void visit(MethodDeclaration declaration, Options context) {
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
                parameter.accept(this, null);

                separator = ", ";
            }
        }

        this.print(")");

        declaration.getBody().accept(this, null);
    }

    public void visit(ParameterDeclaration declaration, Options context) {
        this.print(declaration.getType());
        this.print(" ");
        this.print(declaration.getName());
    }


    // MARK: - Statements

    public void visit(Statement.IfStatement statement, Options context) {
        this.print("if (");
        statement.condition.accept(this, null);
        this.print(") ");

        if (statement.statementIfTrue instanceof Statement.Block) {
            statement.statementIfTrue.accept(this, Options.DO_NOT_PRINT_NEWLINE_AFTER_BLOCK);

            if (statement.statementIfFalse == null) {
                this.println("");
            }
        }
        else {
            this.println("");
            this.indentationDepth += 1;
            statement.statementIfTrue.accept(this, null);
            this.indentationDepth -= 1;
        }

        if (statement.statementIfFalse != null) {
            if (statement.statementIfFalse instanceof Statement.Block) {
                this.print(" else ");
                statement.statementIfFalse.accept(this, null);
            }
            else if (statement.statementIfFalse instanceof Statement.IfStatement) {
                this.print(" else ");
                statement.statementIfFalse.accept(this, null);
            }
            else if (!(statement.statementIfFalse instanceof Statement.EmptyStatement)) {
                this.print(" else ");
                this.indentationDepth += 1;
                statement.statementIfFalse.accept(this, null);
                this.indentationDepth -= 1;
            }
        }
    }

    public void visit(Statement.WhileStatement statement, Options context) {
        this.print("while (");
        statement.condition.accept(this, null);
        this.print(") ");

        if (statement.statementWhileTrue instanceof Statement.Block) {
            statement.statementWhileTrue.accept(this, null);
        }
        else {
            this.println("");
            this.indentationDepth += 1;
            statement.statementWhileTrue.accept(this, null);
            this.indentationDepth -= 1;
        }
    }

    public void visit(Statement.ExpressionStatement statement, Options context) {
        statement.expression.accept(this, null);
        this.println(";");
    }

    public void visit(Statement.ReturnStatement statement, Options context) {
        if (statement.value != null) {
            this.print("return ");
            statement.value.accept(this, null);
            this.println(";");
        }
        else {
            this.println("return;");
        }
    }

    public void visit(Statement.EmptyStatement statement, Options context) {
        this.println(";");
    }

    public void visit(Statement.Block block, Options context) {
        if (block.statements.isEmpty()) {
            if (context == Options.DO_NOT_PRINT_NEWLINE_AFTER_BLOCK) {
                this.print("{ }");
            }
            else {
                this.println("{ }");
            }
        }
        else {
            this.beginBlock();

            for (Statement statement : block.statements) {
                statement.accept(this, null);
            }

            if (context == Options.DO_NOT_PRINT_NEWLINE_AFTER_BLOCK) {
                this.endBlock(false);
            }
            else {
                this.endBlock(true);
            }
        }
    }

    public void visit(Statement.LocalVariableDeclarationStatement statement, Options context) {
        this.print(statement.type);
        this.print(" ");
        this.print(statement.name);

        if (statement.value != null) {
            this.print(" = ");
            statement.value.accept(this, null);
            this.println(";");
        }
        else {
            this.println(";");
        }
    }

    public void visit(Expression.BinaryOperation expression, Options context) {}
    public void visit(Expression.UnaryOperation expression, Options context) {}
    public void visit(Expression.NullLiteral expression, Options context) {}
    public void visit(Expression.BooleanLiteral expression, Options context) {}
    public void visit(Expression.IntegerLiteral expression, Options context) {}
    public void visit(Expression.MethodInvocation expression, Options context) {}
    public void visit(Expression.ExplicitFieldAccess expression, Options context) {}
    public void visit(Expression.ArrayElementAccess expression, Options context) {}
    public void visit(Expression.VariableAccess expression, Options context) {}
    public void visit(Expression.CurrentContextAccess expression, Options context) {}
    public void visit(Expression.NewObjectCreation expression, Options context) {}
    public void visit(Expression.NewArrayCreation expression, Options context) {}

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
        this.print("{");
        this.indentationDepth += 1;
        this.printNewline();
    }

    private void endBlock(boolean newline) {
        this.indentationDepth -= 1;
        this.print("}");

        if (newline) {
            this.printNewline();
        }
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
