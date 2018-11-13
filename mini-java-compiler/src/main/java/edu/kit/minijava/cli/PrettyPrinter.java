package edu.kit.minijava.cli;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public final class PrettyPrinter extends ASTVisitor<PrettyPrinter.Options> {

    PrettyPrinter() {
    }

    public enum Options {
        DO_NOT_PRINT_NEWLINE_AFTER_BLOCK,
        DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION;
    }


    // MARK: - Classes

    String format(Program program) {
        this.builder.setLength(0);

        program.accept(this);

        return this.builder.toString();
    }

    @Override
    protected void visit(Program program, Options context) {
        List<ClassDeclaration> declarations = new ArrayList<>(program.getClassDeclarations());
        declarations.sort(Comparator.comparing(ClassDeclaration::getName));

        for (ClassDeclaration declaration : declarations) {
            declaration.accept(this);
        }
    }

    @Override
    protected void visit(ClassDeclaration declaration, Options context) {
        this.print("class ");
        this.print(declaration.getName());
        this.print(" ");
        this.beginBlock();

        List<SubroutineDeclaration> methods = new ArrayList<>(declaration.getMethodDeclarations());
        methods.addAll(declaration.getMainMethodDeclarations());
        methods.sort(Comparator.comparing(SubroutineDeclaration::getName));

        List<FieldDeclaration> fields = new ArrayList<>(declaration.getFieldDeclarations());
        fields.sort(Comparator.comparing(FieldDeclaration::getName));

        for (SubroutineDeclaration method : methods) {
            if (method instanceof MethodDeclaration) {
                ((MethodDeclaration)method).accept(this);
            }
            else if (method instanceof MainMethodDeclaration) {
                ((MainMethodDeclaration)method).accept(this);
            }
            else {
                throw new AssertionError();
            }
        }

        for (FieldDeclaration field : fields) {
            field.accept(this);
        }

        this.endBlock(true);
    }

    @Override
    protected void visit(FieldDeclaration declaration, Options context) {
        this.print("public ");
        declaration.getType().accept(this);
        this.print(" ");
        this.print(declaration.getName());
        this.println(";");
    }

    @Override
    protected void visit(MethodDeclaration declaration, Options context) {
        this.print("public ");
        declaration.getReturnType().accept(this);
        this.print(" ");
        this.print(declaration.getName());
        this.print("(");

        if (!declaration.getParameterTypes().isEmpty()) {
            String separator = "";

            for (VariableDeclaration parameter : declaration.getParameters()) {
                this.print(separator);
                parameter.accept(this);

                separator = ", ";
            }
        }

        this.print(") ");

        declaration.getBody().accept(this);
    }

    @Override
    protected void visit(MainMethodDeclaration declaration, Options context) {
        this.print("public static void ");
        this.print(declaration.getName());
        this.print("(String[] ");
        this.print(declaration.getArgumentsParameter().getName());
        this.print(") ");

        declaration.getBody().accept(this);
    }

    @Override
    protected void visit(ParameterDeclaration declaration, Options context) {
        declaration.getType().accept(this);
        this.print(" ");
        this.print(declaration.getName());
    }

    @Override
    protected void visit(ExplicitTypeReference reference, Options context) {
        this.print(reference.getBasicTypeReference().getName());

        for (int index = 0; index < reference.getNumberOfDimensions(); index += 1) {
            this.print("[]");
        }
    }

    @Override
    protected void visit(ImplicitTypeReference reference, Options context) {
        throw new AssertionError("This node should not be visited!");
    }


    // MARK: - Statements

    @Override
    protected void visit(Statement.IfStatement statement, Options context) {
        this.print("if (");
        statement.getCondition().accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.print(") ");

        if (statement.getStatementIfTrue() instanceof Statement.Block) {
            statement.getStatementIfTrue().accept(this, Options.DO_NOT_PRINT_NEWLINE_AFTER_BLOCK);

            if (this.shouldPrintElseStatementForIf(statement)) {
                this.print(" ");
            }
            else {
                this.println("");
            }
        }
        else {
            this.println("");
            this.indentationDepth += 1;
            statement.getStatementIfTrue().accept(this);
            this.indentationDepth -= 1;
        }

        if (this.shouldPrintElseStatementForIf(statement)) {
            Statement statementIfFalse = statement.getStatementIfFalse().orElseThrow(AssertionError::new);

            if (statementIfFalse instanceof Statement.Block) {
                this.print("else ");
                statementIfFalse.accept(this);
            }
            else if (statementIfFalse instanceof Statement.IfStatement) {
                this.print("else ");
                statementIfFalse.accept(this);
            }
            else {
                this.print("else ");
                this.indentationDepth += 1;
                statementIfFalse.accept(this);
                this.indentationDepth -= 1;
            }
        }
    }

    private boolean shouldPrintElseStatementForIf(Statement.IfStatement statement) {
        if (!statement.getStatementIfFalse().isPresent()) return false;

        return !(statement.getStatementIfFalse().get() instanceof Statement.EmptyStatement);
    }

    @Override
    protected void visit(Statement.WhileStatement statement, Options context) {
        this.print("while (");
        statement.getCondition().accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.print(") ");

        if (statement.getStatementWhileTrue() instanceof Statement.Block) {
            statement.getStatementWhileTrue().accept(this);
        }
        else {
            this.println("");
            this.indentationDepth += 1;
            statement.getStatementWhileTrue().accept(this);
            this.indentationDepth -= 1;
        }
    }

    @Override
    protected void visit(Statement.ExpressionStatement statement, Options context) {
        statement.getExpression().accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.println(";");
    }

    @Override
    protected void visit(Statement.ReturnStatement statement, Options context) {
        if (statement.getValue().isPresent()) {
            this.print("return ");
            statement.getValue().get().accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
            this.println(";");
        }
        else {
            this.println("return;");
        }
    }

    @Override
    protected void visit(Statement.EmptyStatement statement, Options context) {
        this.println(";");
    }

    @Override
    protected void visit(Statement.Block block, Options context) {
        List<Statement> statements = new ArrayList<>(block.getStatements());
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
                statement.accept(this);
            }

            if (context == Options.DO_NOT_PRINT_NEWLINE_AFTER_BLOCK) {
                this.endBlock(false);
            }
            else {
                this.endBlock(true);
            }
        }
    }

    @Override
    protected void visit(Statement.LocalVariableDeclarationStatement statement, Options context) {
        statement.getType().accept(this);
        this.print(" ");
        this.print(statement.getName());

        if (statement.getValue().isPresent()) {
            this.print(" = ");
            statement.getValue().get().accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
            this.println(";");
        }
        else {
            this.println(";");
        }
    }


    // MARK: - Expressions

    @Override
    protected void visit(Expression.BinaryOperation expression, Options context) {
        this.printOpeningParenthesis(context);
        expression.getLeft().accept(this);
        this.print(" ");
        this.print(expression.getOperationType().getOperatorSymbol());
        this.print(" ");
        expression.getRight().accept(this);
        this.printClosingParenthesis(context);
    }

    @Override
    protected void visit(Expression.UnaryOperation expression, Options context) {
        this.printOpeningParenthesis(context);
        this.print(expression.getOperationType().getOperatorSymbol());
        expression.getOther().accept(this);
        this.printClosingParenthesis(context);
    }

    @Override
    protected void visit(Expression.NullLiteral expression, Options context) {
        this.print("null");
    }

    @Override
    protected void visit(Expression.BooleanLiteral expression, Options context) {
        this.print(expression.getValue() ? "true" : "false");
    }

    @Override
    protected void visit(Expression.IntegerLiteral expression, Options context) {
        this.print(expression.getValue());
    }

    @Override
    protected void visit(Expression.MethodInvocation expression, Options context) {
        this.printOpeningParenthesis(context);

        if (expression.getContext().isPresent()) {
            expression.getContext().get().accept(this);
            this.print(".");
        }

        this.print(expression.getMethodReference().getName());
        this.print("(");

        if (!expression.getArguments().isEmpty()) {
            String separator = "";

            for (Expression argument : expression.getArguments()) {
                this.print(separator);
                argument.accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);

                separator = ", ";
            }
        }

        this.print(")");
        this.printClosingParenthesis(context);
    }

    @Override
    protected void visit(Expression.ExplicitFieldAccess expression, Options context) {
        this.printOpeningParenthesis(context);
        expression.getContext().accept(this);
        this.print(".");
        this.print(expression.getFieldReference().getName());
        this.printClosingParenthesis(context);
    }

    @Override
    protected void visit(Expression.ArrayElementAccess expression, Options context) {
        this.printOpeningParenthesis(context);
        expression.getContext().accept(this);
        this.print("[");
        expression.getIndex().accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.print("]");
        this.printClosingParenthesis(context);
    }

    @Override
    protected void visit(Expression.VariableAccess expression, Options context) {
        this.print(expression.getVariableReference().getName());
    }

    @Override
    protected void visit(Expression.CurrentContextAccess expression, Options context) {
        this.print("this");
    }

    @Override
    protected void visit(Expression.NewObjectCreation expression, Options context) {
        this.printOpeningParenthesis(context);
        this.print("new ");
        this.print(expression.getClassReference().getName());
        this.print("()");
        this.printClosingParenthesis(context);
    }

    @Override
    protected void visit(Expression.NewArrayCreation expression, Options context) {
        this.printOpeningParenthesis(context);
        this.print("new ");
        this.print(expression.getBasicTypeReference().getName());
        this.print("[");
        expression.getPrimaryDimension().accept(this, Options.DO_NOT_PRINT_PARENTHESES_AROUND_EXPRESSION);
        this.print("]");

        for (int index = 1; index < expression.getNumberOfDimensions(); index += 1) {
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
}
