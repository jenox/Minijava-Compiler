package edu.kit.minijava.ast2;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Expression {
    private Expression() {
        this.type = null;
        throw new UnsupportedOperationException();
    }

    public final TypeReference type;

    public static final class BinaryOperation extends Expression {
        public BinaryOperation(BinaryOperationType operationType, Expression left, Expression right) {
            this.operationType = operationType;
            this.left = left;
            this.right = right;
        }

        public final BinaryOperationType operationType;
        public final Expression left;
        public final Expression right;
    }

    public static final class UnaryOperation extends Expression {
        public UnaryOperation(UnaryOperationType operationType, Expression other) {
            this.operationType = operationType;
            this.other = other;
        }

        public final UnaryOperationType operationType;
        public final Expression other;
    }

    public static final class NullLiteral extends Expression {
        public NullLiteral() {
        }
    }

    public static final class BooleanLiteral extends Expression {
        public BooleanLiteral(boolean value) {
            this.value = value;
        }

        public final boolean value;
    }

    public static final class IntegerLiteral extends Expression {
        public IntegerLiteral(String value) {
            this.value = value;
        }

        public final String value;
    }

    public static final class MethodInvocation extends Expression {
        public MethodInvocation(Expression context, String methodName, List<Expression> arguments) {
            List<TypeReference> argumentTypes = arguments.stream().map(e -> e.type).collect(Collectors.toList());

            this.context = context;
            this.reference = new MethodReference(context.type, methodName, argumentTypes);
            this.arguments = arguments;
        }

        public final Expression context; // nullable
        public final MethodReference reference;
        public final List<Expression> arguments;
    }

    public static final class ExplicitFieldAccess extends Expression {
        public ExplicitFieldAccess(Expression context, String fieldName) {
            this.context = context;
            this.reference = new FieldReference(context.type, fieldName);
        }

        public final Expression context;
        public final FieldReference reference;
    }

    public static final class ArrayElementAccess extends Expression {
        public ArrayElementAccess(Expression context, Expression index) {
            this.context = context;
            this.index = index;
        }

        public final Expression context;
        public final Expression index;
    }

    public static final class VariableAccess extends Expression {
        public VariableAccess(String variableName) {
            this.reference = new VariableReference(variableName);
        }

        public final VariableReference reference;
    }

    public static final class CurrentContextAccess extends Expression {
        public CurrentContextAccess() {
        }
    }

    public static final class NewObjectCreation extends Expression {
        public NewObjectCreation(String className) {
            this.reference = new ClassReference(className);
        }

        public final ClassReference reference;
    }

    public static final class NewArrayCreation extends Expression {
        public NewArrayCreation(BasicTypeReference reference, Expression primaryDimension, int numberOfDimensions) {
            this.reference = reference;
            this.primaryDimension = primaryDimension;
            this.numberOfDimensions = numberOfDimensions;
        }

        public final BasicTypeReference reference;
        public final Expression primaryDimension;
        public final int numberOfDimensions;
    }
}
