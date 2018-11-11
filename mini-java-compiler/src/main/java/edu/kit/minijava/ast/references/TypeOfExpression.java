package edu.kit.minijava.ast.references;

public class TypeOfExpression {

    public enum Type {
        BOOLEAN, INT, NULL, TYPE_REF, UNKNOWN
    }

    public static final String INT_NAME = "int";
    public static final String BOOLEAN_NAME = "boolean";

    private Type type;
    private TypeReference reference;

    public TypeOfExpression() {
        this.type = Type.UNKNOWN;
    }

    /**
     * resolve type of expression to given type
     *
     * @param type type expression should be resolved to
     */
    public void resolveTo(Type type) {
        if (type == Type.UNKNOWN) {
            throw new IllegalArgumentException();
        }

        this.type = type;
        this.reference = null;
    }

    /**
     * resolve type of expression
     *
     * @param reference type of expression
     */
    public void resolveTo(TypeReference reference) {
        if (reference == null) {
            throw new IllegalArgumentException();
        }

        this.reference = reference;

        switch (reference.getName()) {
            case INT_NAME:
                this.type = Type.INT;
                break;
            case BOOLEAN_NAME:
                this.type = Type.BOOLEAN;
                break;
            default:
                this.type = Type.TYPE_REF;
                this.reference = reference;
        }


    }

    public Type getType() {
        return this.type;
    }

    public TypeReference getReference() {
        return this.reference;
    }
}
