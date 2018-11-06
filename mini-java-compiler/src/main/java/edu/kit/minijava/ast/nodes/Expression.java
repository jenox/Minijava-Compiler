package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

import java.util.*;
import java.util.stream.*;

public abstract class Expression extends ASTNode {
    private Expression(TypeReference type) {
        this.type = type;
    }

    private final TypeReference type;

    public TypeReference getType() {
        return this.type;
    }

    public static final class BinaryOperation extends Expression {
        public BinaryOperation(BinaryOperationType operationType, Expression left, Expression right) {
            super(new TypeReference());

            this.operationType = operationType;
            this.left = left;
            this.right = right;
        }

        private final BinaryOperationType operationType;
        private final Expression left;
        private final Expression right;

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
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class UnaryOperation extends Expression {
        public UnaryOperation(UnaryOperationType operationType, Expression other) {
            super(new TypeReference());

            this.operationType = operationType;
            this.other = other;
        }

        private final UnaryOperationType operationType;
        private final Expression other;

        public UnaryOperationType getOperationType() {
            return this.operationType;
        }

        public Expression getOther() {
            return this.other;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class NullLiteral extends Expression {
        public NullLiteral() {
            super(new TypeReference());
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class BooleanLiteral extends Expression {
        public BooleanLiteral(boolean value) {
            super(new TypeReference(new BasicTypeReference(PrimitiveTypeDeclaration.BOOLEAN), 0));

            this.value = value;
        }

        private final boolean value;

        public boolean getValue() {
            return this.value;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class IntegerLiteral extends Expression {
        public IntegerLiteral(String value) {
            super(new TypeReference(new BasicTypeReference(PrimitiveTypeDeclaration.INTEGER), 0));

            this.value = value;
        }

        private final String value;

        public String getValue() {
            return this.value;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class MethodInvocation extends Expression {
        public MethodInvocation(Expression context, String methodName, List<Expression> arguments) {
            super(new TypeReference());

            List<TypeReference> argumentTypes = arguments.stream().map(e -> e.type).collect(Collectors.toList());

            this.context = context;
            this.arguments = arguments;

            if (context != null) {
                this.reference = new MethodReference(context.type, methodName, argumentTypes);
            }
            else {
                this.reference = new MethodReference(null, methodName, argumentTypes);
            }
        }

        private final Expression context; // nullable
        private final MethodReference reference;
        private final List<Expression> arguments;

        public Expression getContext() {
            return this.context;
        }

        public MethodReference getReference() {
            return this.reference;
        }

        public List<Expression> getArguments() {
            return this.arguments;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class ExplicitFieldAccess extends Expression {
        public ExplicitFieldAccess(Expression context, String fieldName) {
            super(new TypeReference());

            this.context = context;
            this.reference = new FieldReference(context.type, fieldName);
        }

        private final Expression context;
        private final FieldReference reference;

        public Expression getContext() {
            return this.context;
        }

        public FieldReference getReference() {
            return this.reference;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class ArrayElementAccess extends Expression {
        public ArrayElementAccess(Expression context, Expression index) {
            super(new TypeReference());

            this.context = context;
            this.index = index;
        }

        private final Expression context;
        private final Expression index;

        public Expression getContext() {
            return this.context;
        }

        public Expression getIndex() {
            return this.index;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class VariableAccess extends Expression {
        public VariableAccess(String variableName) {
            super(new TypeReference());

            this.reference = new VariableReference(variableName);
        }

        private final VariableReference reference;

        public VariableReference getReference() {
            return this.reference;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class CurrentContextAccess extends Expression {
        public CurrentContextAccess() {
            super(new TypeReference());
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class NewObjectCreation extends Expression {
        public NewObjectCreation(String className) {
            super(new TypeReference(new BasicTypeReference(className),0));

            this.reference = new ClassReference(className);
        }

        private final ClassReference reference;

        public ClassReference getReference() {
            return this.reference;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }

    public static final class NewArrayCreation extends Expression {
        public NewArrayCreation(BasicTypeReference reference, Expression primaryDimension, int numberOfDimensions) {
            super(new TypeReference(reference, numberOfDimensions));

            this.reference = reference;
            this.primaryDimension = primaryDimension;
            this.numberOfDimensions = numberOfDimensions;
        }

        private final BasicTypeReference reference;
        private final Expression primaryDimension;
        private final int numberOfDimensions;

        public BasicTypeReference getReference() {
            return this.reference;
        }

        public Expression getPrimaryDimension() {
            return this.primaryDimension;
        }

        public int getNumberOfDimensions() {
            return this.numberOfDimensions;
        }

        @Override
        public <T> void accept(ASTVisitor<T> visitor, T context) {
            visitor.visit(this, context);
        }
    }
}
