package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

import java.util.*;

public abstract class Statement implements ASTNode {
    private Statement() {
    }

    public static final class IfStatement extends Statement {
        public IfStatement(Expression condition, Statement statementIfTrue) {
            this.condition = condition;
            this.statementIfTrue = statementIfTrue;
            this.statementIfFalse = null;
        }

        public IfStatement(Expression condition, Statement statementIfTrue, Statement statementIfFalse) {
            this.condition = condition;
            this.statementIfTrue = statementIfTrue;
            this.statementIfFalse = statementIfFalse;
        }

        private final Expression condition;
        private final Statement statementIfTrue;
        private final Statement statementIfFalse; //nullable

        public Expression getCondition() {
            return this.condition;
        }

        public Statement getStatementIfTrue() {
            return this.statementIfTrue;
        }

        public Statement getStatementIfFalse() {
            return this.statementIfFalse;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class WhileStatement extends Statement {
        public WhileStatement(Expression condition, Statement statementWhileTrue) {
            this.condition = condition;
            this.statementWhileTrue = statementWhileTrue;
        }

        private final Expression condition;
        private final Statement statementWhileTrue;

        public Expression getCondition() {
            return this.condition;
        }

        public Statement getStatementWhileTrue() {
            return this.statementWhileTrue;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class ExpressionStatement extends Statement {
        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        private final Expression expression;

        public Expression getExpression() {
            return this.expression;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class ReturnStatement extends Statement {
        public ReturnStatement() {
            this.value = null;
        }

        public ReturnStatement(Expression value) {
            this.value = value;
        }

        private final Expression value; // nullable

        public Expression getValue() {
            return this.value;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class EmptyStatement extends Statement {
        public EmptyStatement() {
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class Block extends Statement {
        public Block(List<Statement> statements) {
            this.statements = Collections.unmodifiableList(statements);
        }

        private final List<Statement> statements;

        public List<Statement> getStatements() {
            return this.statements;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class LocalVariableDeclarationStatement extends Statement implements VariableDeclaration {
        public LocalVariableDeclarationStatement(TypeReference type, String name) {
            this.type = type;
            this.name = name;
            this.value = null;
        }

        public LocalVariableDeclarationStatement(TypeReference type, String name, Expression value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }

        private final TypeReference type;
        private final String name;
        private final Expression value; //nullable

        @Override
        public TypeReference getType() {
            return this.type;
        }

        public String getName() {
            return this.name;
        }

        public Expression getValue() {
            return this.value;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }
}
