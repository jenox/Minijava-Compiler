package edu.kit.minijava.ast2.nodes;

import edu.kit.minijava.ast2.references.*;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Expression extends ASTNode {
    private Expression(TypeReference type) {
        this.type = type;
    }

    public final TypeReference type;

    public static final class BinaryOperation extends Expression {
        public BinaryOperation(BinaryOperationType operationType, Expression left, Expression right) {
            super(new TypeReference());

            this.operationType = operationType;
            this.left = left;
            this.right = right;
        }

        public final BinaryOperationType operationType;
        public final Expression left;
        public final Expression right;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class UnaryOperation extends Expression {
        public UnaryOperation(UnaryOperationType operationType, Expression other) {
            super(new TypeReference());

            this.operationType = operationType;
            this.other = other;
        }

        public final UnaryOperationType operationType;
        public final Expression other;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class NullLiteral extends Expression {
        public NullLiteral() {
            super(new TypeReference());
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class BooleanLiteral extends Expression {
        public BooleanLiteral(boolean value) {
            super(new TypeReference(new BasicTypeReference(PrimitiveTypeDeclaration.BOOLEAN), 0));

            this.value = value;
        }

        public final boolean value;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class IntegerLiteral extends Expression {
        public IntegerLiteral(String value) {
            super(new TypeReference(new BasicTypeReference(PrimitiveTypeDeclaration.INTEGER), 0));

            this.value = value;
        }

        public final String value;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
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

        public final Expression context; // nullable
        public final MethodReference reference;
        public final List<Expression> arguments;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class ExplicitFieldAccess extends Expression {
        public ExplicitFieldAccess(Expression context, String fieldName) {
            super(new TypeReference());

            this.context = context;
            this.reference = new FieldReference(context.type, fieldName);
        }

        public final Expression context;
        public final FieldReference reference;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class ArrayElementAccess extends Expression {
        public ArrayElementAccess(Expression context, Expression index) {
            super(new TypeReference());

            this.context = context;
            this.index = index;
        }

        public final Expression context;
        public final Expression index;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class VariableAccess extends Expression {
        public VariableAccess(String variableName) {
            super(new TypeReference());

            this.reference = new VariableReference(variableName);
        }

        public final VariableReference reference;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class CurrentContextAccess extends Expression {
        public CurrentContextAccess() {
            super(new TypeReference());
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class NewObjectCreation extends Expression {
        public NewObjectCreation(String className) {
            super(new TypeReference(new BasicTypeReference(className),0));

            this.reference = new ClassReference(className);
        }

        public final ClassReference reference;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static final class NewArrayCreation extends Expression {
        public NewArrayCreation(BasicTypeReference reference, Expression primaryDimension, int numberOfDimensions) {
            super(new TypeReference(reference, numberOfDimensions));

            this.reference = reference;
            this.primaryDimension = primaryDimension;
            this.numberOfDimensions = numberOfDimensions;
        }

        public final BasicTypeReference reference;
        public final Expression primaryDimension;
        public final int numberOfDimensions;

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }
}
