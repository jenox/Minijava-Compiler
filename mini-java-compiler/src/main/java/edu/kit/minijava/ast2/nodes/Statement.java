package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

import java.util.*;

public abstract class Statement extends ASTNode {
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

        public final Expression condition;
        public final Statement statementIfTrue;
        public final Statement statementIfFalse; //nullable

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

        public final Expression condition;
        public final Statement statementWhileTrue;

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class ExpressionStatement extends Statement {
        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        public final Expression expression;

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

        public final Expression value; // nullable

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

        public final List<Statement> statements;

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

        public final TypeReference type;
        public final String name;
        public final Expression value; //nullable

        @Override
        public TypeReference getType() {
            return this.type;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }
}
