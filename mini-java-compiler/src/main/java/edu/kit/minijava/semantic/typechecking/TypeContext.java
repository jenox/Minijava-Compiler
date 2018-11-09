package edu.kit.minijava.semantic.typechecking;

import edu.kit.minijava.ast.references.TypeReference;

public class TypeContext {

    public static final String INT_NAME = "int";
    public static final String BOOLEAN_NAME = "boolean";

    private Reference reference;
    private boolean isNullAlowed;
    private TypeReference typeRef;
    private int numberOfDimensions;

    public TypeContext() {
        this.reference = Reference.UNKNOWN;
    }

    public TypeContext(Reference reference) {
        this.reference = reference;
    }

    public TypeContext(TypeReference ref, int numberOfDimensions) {
        this(ref);
        if (numberOfDimensions > 0) {
            this.reference = Reference.TYPE; //no primitive type
            this.numberOfDimensions = numberOfDimensions;
            this.isNullAlowed = true;
        }
    }

    public TypeContext(TypeReference reference) {
        this.setType(reference);
    }

    public Reference getReference() {
        return this.reference;
    }

    public boolean isNullAlowed() {
        return this.isNullAlowed;
    }

    public int getNumberOfDimensions() {
        return this.numberOfDimensions;
    }

    public TypeReference getTypeRef() {
        return this.typeRef;
    }

    public boolean isCompatible(TypeContext context) {
        if (this.isUnknown() || context.isUnknown()) {
            return false; // we do not know if these types are compatible
        }

        if (this.reference == context.reference) {
            return true;
        }
        // if types are unequal the can still be compatible, if one value is NULL while the
        // other one accepts null
        else {
            return (this.isNullAlowed && context.isNull() || (this.isNull() && context.isNullAlowed));
        }
    }

    public void setArithmetic() {
        this.reference = Reference.ARTIHMETIC;
        this.isNullAlowed = false;
        this.numberOfDimensions = 0;
    }

    public void setBoolean() {
        this.reference = Reference.BOOLEAN;
        this.isNullAlowed = false;
        this.numberOfDimensions = 0;
    }

    public void setNull() {
        this.reference = Reference.NULL;
        this.isNullAlowed = true;
        this.numberOfDimensions = 0;
    }

    public void setType(TypeReference ref) {

        switch (ref.getName()) {
            case INT_NAME:
                this.reference = Reference.ARTIHMETIC;
                this.isNullAlowed = false;
                this.numberOfDimensions = 0;
                break;
            case BOOLEAN_NAME:
                this.reference = Reference.BOOLEAN;
                this.isNullAlowed = false;
                this.numberOfDimensions = 0;
                break;
            default:
                this.reference = Reference.TYPE;
                this.isNullAlowed = true;
                this.numberOfDimensions = ref.getNumberOfDimensions();
        }

    }

    public void reduceDimension() {
        this.numberOfDimensions = this.numberOfDimensions > 0 ? this.numberOfDimensions - 1 : 0;
    }

    public boolean isBoolean() {
        return this.reference.equals(Reference.BOOLEAN);
    }

    public boolean isArithmetic() {
        return this.reference.equals(Reference.ARTIHMETIC);
    }

    public boolean isVoid() {
        return this.reference.equals(Reference.VOID);
    }

    public boolean isUnknown() {
        return this.reference.equals(Reference.UNKNOWN);
    }

    public boolean isNull() {
        return this.reference.equals(Reference.NULL);
    }

    public static TypeContext makeBoolean() {
        return new TypeContext(Reference.BOOLEAN);
    }

    public static TypeContext makeVoid() {
        return new TypeContext(Reference.VOID);
    }

    public static TypeContext makeArithmetic() {
        return new TypeContext(Reference.ARTIHMETIC);
    }

}
