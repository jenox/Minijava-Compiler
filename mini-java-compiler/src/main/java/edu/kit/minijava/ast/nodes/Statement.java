package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.lexer.*;

import java.util.*;

public abstract class Statement implements ASTNode {
    private Statement() {
    }

    public abstract TokenLocation getLocation();
    public abstract boolean explicitlyReturns();
    public abstract boolean containsUnreachableStatements();

    public static final class IfStatement extends Statement {
        public IfStatement(Expression condition, Statement statementIfTrue, TokenLocation location) {
            this.condition = condition;
            this.statementIfTrue = statementIfTrue;
            this.statementIfFalse = null;
            this.location = location;
        }

        public IfStatement(Expression condition, Statement statementIfTrue, Statement statementIfFalse,
                           TokenLocation location) {
            this.condition = condition;
            this.statementIfTrue = statementIfTrue;
            this.statementIfFalse = statementIfFalse;
            this.location = location;
        }

        private Expression condition;
        private final Statement statementIfTrue;
        private final Statement statementIfFalse; //nullable
        private final TokenLocation location;

        public Expression getCondition() {
            return this.condition;
        }

        public Statement getStatementIfTrue() {
            return this.statementIfTrue;
        }

        public Optional<Statement> getStatementIfFalse() {
            return Optional.ofNullable(this.statementIfFalse);
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean explicitlyReturns() {
            if (this.statementIfFalse == null) {
                return false;
            }
            else {
                return this.statementIfTrue.explicitlyReturns() && this.statementIfFalse.explicitlyReturns();
            }
        }

        @Override
        public boolean containsUnreachableStatements() {
            if (this.statementIfTrue.containsUnreachableStatements()) {
                return true;
            }
            else if (this.statementIfFalse != null) {
                return this.statementIfFalse.containsUnreachableStatements();
            }
            else {
                return false;
            }
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.condition == oldValue) {
                this.condition = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "If";
        }
    }

    public static final class WhileStatement extends Statement {
        public WhileStatement(Expression condition, Statement statementWhileTrue, TokenLocation location) {
            this.condition = condition;
            this.statementWhileTrue = statementWhileTrue;
            this.location = location;
        }

        private Expression condition;
        private final Statement statementWhileTrue;
        private final TokenLocation location;

        public Expression getCondition() {
            return this.condition;
        }

        public Statement getStatementWhileTrue() {
            return this.statementWhileTrue;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean explicitlyReturns() {
            return false;
        }

        @Override
        public boolean containsUnreachableStatements() {
            return this.statementWhileTrue.containsUnreachableStatements();
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.condition == oldValue) {
                this.condition = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "While";
        }
    }

    public static final class ExpressionStatement extends Statement {
        public ExpressionStatement(Expression expression, TokenLocation location) {
            this.expression = expression;
            this.location = location;
        }

        private Expression expression;
        private final TokenLocation location;

        public Expression getExpression() {
            return this.expression;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean explicitlyReturns() {
            return false;
        }

        @Override
        public boolean containsUnreachableStatements() {
            return false;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.expression == oldValue) {
                this.expression = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "Expression";
        }
    }

    public static final class ReturnStatement extends Statement {
        public ReturnStatement(TokenLocation location) {
            this.value = null;
            this.location = location;
        }

        public ReturnStatement(Expression value, TokenLocation location) {
            this.value = value;
            this.location = location;
        }

        private Expression value; // nullable
        private final TokenLocation location;

        public Optional<Expression> getValue() {
            return Optional.ofNullable(this.value);
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean explicitlyReturns() {
            return true;
        }

        @Override
        public boolean containsUnreachableStatements() {
            return false;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.value == oldValue) {
                this.value = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "Return";
        }
    }

    public static final class EmptyStatement extends Statement {
        public EmptyStatement(TokenLocation location) {
            this.location = location;
        }

        private final TokenLocation location;

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean explicitlyReturns() {
            return false;
        }

        @Override
        public boolean containsUnreachableStatements() {
            return false;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {}

        @Override
        public String toStringForDumpingAST() {
            return "Empty";
        }
    }

    public static final class Block extends Statement {
        public Block(List<Statement> statements, TokenLocation location) {
            this.statements = Collections.unmodifiableList(statements);
            this.location = location;
        }

        private final List<Statement> statements;
        private final TokenLocation location;

        public List<Statement> getStatements() {
            return this.statements;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean explicitlyReturns() {
            for (Statement statement : this.statements) {
                if (statement.explicitlyReturns()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean containsUnreachableStatements() {
            for (int index = 0; index < this.statements.size(); index += 1) {
                if (this.statements.get(index).explicitlyReturns()) {
                    return index + 1 < this.statements.size();
                }
            }

            return false;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {}

        @Override
        public String toStringForDumpingAST() {
            return "Block";
        }
    }

    public static final class LocalVariableDeclarationStatement extends Statement implements VariableDeclaration {
        public LocalVariableDeclarationStatement(TypeReference type, String name, TokenLocation location) {
            this.type = type;
            this.name = name;
            this.value = null;
            this.location = location;
        }

        public LocalVariableDeclarationStatement(TypeReference type, String name, Expression value,
                                                 TokenLocation location) {
            this.type = type;
            this.name = name;
            this.value = value;
            this.location = location;
        }

        private final TypeReference type;
        private final String name;
        private Expression value; //nullable
        private final TokenLocation location;

        @Override
        public TypeReference getType() {
            return this.type;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public boolean canBeShadowedByVariableDeclarationInNestedScope() {
            return false;
        }

        @Override
        public boolean canBeAccessed() {
            return true;
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        public Optional<Expression> getValue() {
            return Optional.ofNullable(this.value);
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean explicitlyReturns() {
            return false;
        }

        @Override
        public boolean containsUnreachableStatements() {
            return false;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.value == oldValue) {
                this.value = newValue;
            }
        }

        @Override
        public String toString() {
            return "local variable '" + this.name + "' at " + this.location;
        }

        @Override
        public String toStringForDumpingAST() {
            return "Local Variable " + this.name + "\n" + this.location;
        }
    }
}
