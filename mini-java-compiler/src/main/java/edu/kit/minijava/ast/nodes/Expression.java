package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;
import edu.kit.minijava.lexer.*;

import java.util.*;
import java.util.stream.*;

public abstract class Expression implements ASTNode {
    private Expression() {
        this.type = new TypeOfExpression();
    }

    private final TypeOfExpression type;
    private int numberOfExplicitParentheses = 0;

    public TypeOfExpression getType() {
        return this.type;
    }

    public final int getNumberOfExplicitParentheses() {
        return this.numberOfExplicitParentheses;
    }

    public final void setNumberOfExplicitParentheses(int numberOfExplicitParentheses) {
        this.numberOfExplicitParentheses = numberOfExplicitParentheses;
    }

    public final boolean hasExplicitParentheses() {
        return this.numberOfExplicitParentheses > 0;
    }

    public abstract TokenLocation getLocation();
    public abstract boolean isValidForExpressionStatement();

    public static final class BinaryOperation extends Expression {
        public BinaryOperation(BinaryOperationType operationType, Expression left, Expression right,
                               TokenLocation location) {
            super();

            this.operationType = operationType;
            this.left = left;
            this.right = right;
            this.location = location;
        }

        private final BinaryOperationType operationType;
        private Expression left;
        private Expression right;
        private final TokenLocation location;

        public BinaryOperationType getOperationType() {
            return this.operationType;
        }

        public Expression getLeft() {
            return this.left;
        }

        public Expression getRight() {
            return this.right;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
            return this.operationType == BinaryOperationType.ASSIGNMENT;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.left == oldValue) {
                this.left = newValue;
            }

            if (this.right == oldValue) {
                this.right = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            switch (this.operationType) {
                case MULTIPLICATION: return "×";
                case DIVISION: return "÷";
                case MODULO: return "mod";
                case ADDITION: return "+";
                case SUBTRACTION: return "−";
                case LESS_THAN: return "<";
                case LESS_THAN_OR_EQUAL_TO: return "≤";
                case GREATER_THAN: return ">";
                case GREATER_THAN_OR_EQUAL_TO: return "≥";
                case EQUAL_TO: return "=";
                case NOT_EQUAL_TO: return "≠";
                case LOGICAL_AND: return "∧";
                case LOGICAL_OR: return "∨";
                case ASSIGNMENT: return "≔";
                default: throw new AssertionError();
            }
        }
    }

    public static final class UnaryOperation extends Expression {
        public UnaryOperation(UnaryOperationType operationType, Expression other, TokenLocation location) {
            super();

            this.operationType = operationType;
            this.other = other;
            this.location = location;
        }

        private final UnaryOperationType operationType;
        private Expression other;
        private final TokenLocation location;

        public UnaryOperationType getOperationType() {
            return this.operationType;
        }

        public Expression getOther() {
            return this.other;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            if (this.other == oldValue) {
                this.other = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            switch (this.operationType) {
                case NUMERIC_NEGATION: return "±";
                case LOGICAL_NEGATION: return "¬";
                default: throw new AssertionError();
            }
        }
    }

    public static final class NullLiteral extends Expression {
        public NullLiteral(TokenLocation location) {
            super();

            this.location = location;
        }

        private final TokenLocation location;

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            return "null";
        }
    }

    public static final class BooleanLiteral extends Expression {
        public BooleanLiteral(boolean value, TokenLocation location) {
            super();

            this.value = value;
            this.location = location;
        }

        private final boolean value;
        private final TokenLocation location;

        public boolean getValue() {
            return this.value;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            return this.value ? "true" : "false";
        }
    }

    public static final class IntegerLiteral extends Expression {
        public IntegerLiteral(String value, TokenLocation location) {
            super();

            this.value = value;
            this.location = location;
        }

        private final String value;
        private final TokenLocation location;

        public String getValue() {
            return this.value;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            return this.value;
        }
    }

    public static final class MethodInvocation extends Expression {
        public MethodInvocation(String methodName, List<Expression> arguments, TokenLocation location) {
            super();

            this.context = null;
            this.arguments = arguments;
            this.methodReference = new ExplicitReference<>(methodName, location);
            this.location = location;
        }

        public MethodInvocation(Expression context, String methodName, List<Expression> arguments,
                                TokenLocation location) {
            super();

            this.context = context;
            this.arguments = arguments;
            this.methodReference = new ExplicitReference<>(methodName, location);
            this.location = location;
        }

        private Expression context; // nullable
        private final ExplicitReference<MethodDeclaration> methodReference;
        private final List<Expression> arguments;
        private final TokenLocation location;

        public Optional<Expression> getContext() {
            return Optional.ofNullable(this.context);
        }

        public ExplicitReference<MethodDeclaration> getMethodReference() {
            return this.methodReference;
        }

        public List<Expression> getArguments() {
            return Collections.unmodifiableList(this.arguments);
        }

        public List<TypeOfExpression> getArgumentTypes() {
            return this.arguments.stream().map(e -> e.type).collect(Collectors.toList());
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
            return true;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.context == oldValue) {
                this.context = newValue;
            }

            for (int index = 0; index < this.arguments.size(); index += 1) {
                if (this.arguments.get(index) == oldValue) {
                    this.arguments.set(index, newValue);
                }
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "Method Invocation";
        }
    }

    public static final class ExplicitFieldAccess extends Expression {
        public ExplicitFieldAccess(Expression context, String fieldName, TokenLocation location) {
            super();

            this.context = context;
            this.fieldReference = new ExplicitReference<>(fieldName, location);
            this.location = location;
        }

        private Expression context;
        private final ExplicitReference<FieldDeclaration> fieldReference;
        private final TokenLocation location;

        public Expression getContext() {
            return this.context;
        }

        public ExplicitReference<FieldDeclaration> getFieldReference() {
            return this.fieldReference;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            if (this.context == oldValue) {
                this.context = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "Field Access";
        }
    }

    public static final class ArrayElementAccess extends Expression {
        public ArrayElementAccess(Expression context, Expression index, TokenLocation location) {
            super();

            this.context = context;
            this.index = index;
            this.location = location;
        }

        private Expression context;
        private Expression index;
        private final TokenLocation location;

        public Expression getContext() {
            return this.context;
        }

        public Expression getIndex() {
            return this.index;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            if (this.context == oldValue) {
                this.context = newValue;
            }

            if (this.index == oldValue) {
                this.index = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "Array Access";
        }
    }

    public static final class VariableAccess extends Expression {
        public VariableAccess(String variableName, TokenLocation location) {
            super();

            this.variableReference = new ExplicitReference<>(variableName, location);
            this.location = location;
        }

        private final ExplicitReference<VariableDeclaration> variableReference;
        private final TokenLocation location;

        public ExplicitReference<VariableDeclaration> getVariableReference() {
            return this.variableReference;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            return "Variable Access";
        }
    }

    public static final class CurrentContextAccess extends Expression {
        public CurrentContextAccess(TokenLocation location) {
            super();

            this.location = location;
        }

        private final TokenLocation location;

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            return "this";
        }
    }

    public static final class NewObjectCreation extends Expression {
        public NewObjectCreation(String className, TokenLocation location) {
            super();

            this.classReference = new ExplicitReference<>(className, location);
            this.location = location;
        }

        private final ExplicitReference<ClassDeclaration> classReference;
        private final TokenLocation location;

        public ExplicitReference<ClassDeclaration> getClassReference() {
            return this.classReference;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            return "Object Creation";
        }
    }

    public static final class NewArrayCreation extends Expression {
        public NewArrayCreation(ExplicitReference<BasicTypeDeclaration> basicTypeReference, Expression primaryDimension,
                                int numberOfDimensions, TokenLocation location) {
            super();

            this.basicTypeReference = basicTypeReference;
            this.primaryDimension = primaryDimension;
            this.numberOfDimensions = numberOfDimensions;
            this.location = location;
        }

        private final ExplicitReference<BasicTypeDeclaration> basicTypeReference;
        private Expression primaryDimension;
        private final int numberOfDimensions;
        private final TokenLocation location;

        public ExplicitReference<BasicTypeDeclaration> getBasicTypeReference() {
            return this.basicTypeReference;
        }

        public Expression getPrimaryDimension() {
            return this.primaryDimension;
        }

        public int getNumberOfDimensions() {
            return this.numberOfDimensions;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
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
            if (this.primaryDimension == oldValue) {
                this.primaryDimension = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "Array Creation";
        }
    }

    public static final class SystemOutPrintlnExpression extends Expression {
        public SystemOutPrintlnExpression(Expression argument, TokenLocation location) {
            this.argument = argument;
            this.location = location;
        }

        private Expression argument;
        private final TokenLocation location;

        public Expression getArgument() {
            return this.argument;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
            return true;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.argument == oldValue) {
                this.argument = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "println";
        }
    }

    public static final class SystemOutFlushExpression extends Expression {
        public SystemOutFlushExpression(TokenLocation location) {
            this.location = location;
        }

        private final TokenLocation location;

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
            return true;
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
            return "flush";
        }
    }

    public static final class SystemOutWriteExpression extends Expression {
        public SystemOutWriteExpression(Expression argument, TokenLocation location) {
            this.argument = argument;
            this.location = location;
        }

        private Expression argument;
        private final TokenLocation location;

        public Expression getArgument() {
            return this.argument;
        }

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
            return true;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.willVisit(this);
            visitor.visit(this, context);
            visitor.didVisit(this);
        }

        @Override
        public void substituteExpression(Expression oldValue, Expression newValue) {
            if (this.argument == oldValue) {
                this.argument = newValue;
            }
        }

        @Override
        public String toStringForDumpingAST() {
            return "write";
        }
    }

    public static final class SystemInReadExpression extends Expression {
        public SystemInReadExpression(TokenLocation location) {
            this.location = location;
        }

        private final TokenLocation location;

        @Override
        public TokenLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean isValidForExpressionStatement() {
            return true;
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
            return "read";
        }
    }
}
