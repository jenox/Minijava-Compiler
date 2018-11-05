package edu.kit.minijava.ast2;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Statement {
    private Statement() {
    }

    public final class IfStatement extends Statement {
        public IfStatement(Expression condition, Statement statementIfTrue, Statement statementIfFalse) {
            this.condition = condition;
            this.statementIfTrue = statementIfTrue;
            this.statementIfFalse = statementIfFalse;
        }

        public final Expression condition;
        public final Statement statementIfTrue;
        public final Statement statementIfFalse; //nullable
    }

    public final class WhileStatement extends Statement {
        public WhileStatement(Expression condition, Statement statementWhileTrue) {
            this.condition = condition;
            this.statementWhileTrue = statementWhileTrue;
        }

        public final Expression condition;
        public final Statement statementWhileTrue;
    }

    public final class ExpressionStatement extends Statement {
        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        public final Expression expression;
    }

    public final class ReturnStatement extends Statement {
        public ReturnStatement(Expression value) {
            this.value = value;
        }

        public final Expression value; // nullable
    }

    public final class EmptyStatement extends Statement {
        public EmptyStatement() {
        }
    }

    public final class Block extends Statement {
        public Block(List<Statement> statements) {
            this.statements = Collections.unmodifiableList(statements);
        }

        public final List<Statement> statements;
    }

    public final class LocalVariableDeclarationStatement extends Statement implements VariableDeclaration {
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
    }
}
