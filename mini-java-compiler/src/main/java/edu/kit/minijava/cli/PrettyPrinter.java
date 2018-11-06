package edu.kit.minijava.cli;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;

public class PrettyPrinter implements ASTVisitor<PrettyPrinter.Options> {

    public PrettyPrinter() {
    }

    public enum Options {
        DO_NOT_PRINT_NEWLINE_AFTER_BLOCK,
        DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION;
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
        this.print("class ");
        this.print(declaration.getName());
        this.print(" ");
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

        this.print(") ");

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
        statement.condition.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.print(") ");

        if (statement.statementIfTrue instanceof Statement.Block) {
            statement.statementIfTrue.accept(this, Options.DO_NOT_PRINT_NEWLINE_AFTER_BLOCK);

            if (this.shouldPrintElseStatement(statement.statementIfFalse)) {
                this.print(" ");
            }
            else {
                this.println("");
            }
        }
        else {
            this.println("");
            this.indentationDepth += 1;
            statement.statementIfTrue.accept(this, null);
            this.indentationDepth -= 1;
        }

        if (this.shouldPrintElseStatement(statement.statementIfFalse)) {
            if (statement.statementIfFalse instanceof Statement.Block) {
                this.print("else ");
                statement.statementIfFalse.accept(this, null);
            }
            else if (statement.statementIfFalse instanceof Statement.IfStatement) {
                this.print("else ");
                statement.statementIfFalse.accept(this, null);
            }
            else {
                this.print("else ");
                this.indentationDepth += 1;
                statement.statementIfFalse.accept(this, null);
                this.indentationDepth -= 1;
            }
        }
    }

    private boolean shouldPrintElseStatement(Statement statement) {
        if (statement == null) return false;
        if (statement instanceof Statement.EmptyStatement) return false;

        return true;
    }

    public void visit(Statement.WhileStatement statement, Options context) {
        this.print("while (");
        statement.condition.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
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
        statement.expression.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.println(";");
    }

    public void visit(Statement.ReturnStatement statement, Options context) {
        if (statement.value != null) {
            this.print("return ");
            statement.value.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
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
        List<Statement> statements = new ArrayList<>(block.statements);
        statements.removeIf(s -> s instanceof Statement.EmptyStatement);

        if (statements.isEmpty()) {
            if (context == Options.DO_NOT_PRINT_NEWLINE_AFTER_BLOCK) {
                this.print("{ }");
            }
            else {
                this.println("{ }");
            }
        }
        else {
            this.beginBlock();

            for (Statement statement : statements) {
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
            statement.value.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
            this.println(";");
        }
        else {
            this.println(";");
        }
    }


    // MARK: - Expressions

    public void visit(Expression.BinaryOperation expression, Options context) {
        this.printOpeningParenthesis(context);
        expression.left.accept(this, null);
        this.print(" ");
        this.print(expression.operationType.operatorSymbol);
        this.print(" ");
        expression.right.accept(this, null);
        this.printClosingParenthesis(context);
    }

    public void visit(Expression.UnaryOperation expression, Options context) {
        this.printOpeningParenthesis(context);
        this.print(expression.operationType.operatorSymbol);
        expression.other.accept(this, null);
        this.printClosingParenthesis(context);
    }

    public void visit(Expression.NullLiteral expression, Options context) {
        this.print("null");
    }

    public void visit(Expression.BooleanLiteral expression, Options context) {
        this.print(expression.value ? "true" : "false");
    }

    public void visit(Expression.IntegerLiteral expression, Options context) {
        this.print(expression.value);
    }

    public void visit(Expression.MethodInvocation expression, Options context) {
        this.printOpeningParenthesis(context);

        if (expression.context != null) {
            expression.context.accept(this, null);
            this.print(".");
        }

        this.print(expression.reference.name);
        this.print("(");

        if (!expression.arguments.isEmpty()) {
            String separator = "";

            for (Expression argument : expression.arguments) {
                this.print(separator);
                argument.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);

                separator = ", ";
            }
        }

        this.print(")");
        this.printClosingParenthesis(context);
    }

    public void visit(Expression.ExplicitFieldAccess expression, Options context) {
        this.printOpeningParenthesis(context);
        expression.context.accept(this, null);
        this.print(".");
        this.print(expression.reference.name);
        this.printClosingParenthesis(context);
    }

    public void visit(Expression.ArrayElementAccess expression, Options context) {
        this.printOpeningParenthesis(context);
        expression.context.accept(this, null);
        this.print("[");
        expression.index.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.print("]");
        this.printClosingParenthesis(context);
    }

    public void visit(Expression.VariableAccess expression, Options context) {
        this.print(expression.reference.name);
    }

    public void visit(Expression.CurrentContextAccess expression, Options context) {
        this.print("this");
    }

    public void visit(Expression.NewObjectCreation expression, Options context) {
        this.printOpeningParenthesis(context);
        this.print("new ");
        this.print(expression.reference.name);
        this.print("()");
        this.printClosingParenthesis(context);
    }

    public void visit(Expression.NewArrayCreation expression, Options context) {
        this.printOpeningParenthesis(context);
        this.print("new ");
        this.print(expression.reference.name);
        this.print("[");
        expression.primaryDimension.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.print("]");

        for (int index = 1; index < expression.numberOfDimensions; index += 1) {
            this.print("[]");
        }

        this.printClosingParenthesis(context);
    }

    private void printOpeningParenthesis(Options context) {
        if (context != Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION) {
            this.print("(");
        }
    }

    private void printClosingParenthesis(Options context) {
        if (context != Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION) {
            this.print(")");
        }
    }


    // MARK: - Output

    private final StringBuilder builder = new StringBuilder();

    private int indentationDepth = 0;
    private boolean hasPrintedIndentationForCurrentLine = false;

    private void print(String string) {
        assert string != null;

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
}
