package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.references.*;

public class TypeContext {

    public static final String INT_NAME = "int";
    public static final String BOOLEAN_NAME = "boolean";

    private Reference reference;
    private boolean isNullAllowed;
    private TypeReference typeRef;
    private int numberOfDimensions;

    public TypeContext() {
        this.reference = Reference.UNKNOWN;
    }

    public TypeContext(Reference reference) {
        this.reference = reference;
    }

    public TypeContext(TypeReference ref, int numberOfDimensions) {
        this.setType(ref);

        if (numberOfDimensions > 0) {
            this.reference = Reference.TYPE; //no primitive type
            this.numberOfDimensions = numberOfDimensions;
            this.isNullAllowed = true;
            this.typeRef = ref;
        }
    }

    public TypeContext(TypeReference reference) {
        this.setType(reference);

    }

    /**
     * Copy constructor for a type context.
     *
     * @param another A type context object to be copied.
     */
    public TypeContext(TypeContext another) {
        this.reference = another.reference;
        this.isNullAllowed = another.isNullAllowed;
        this.typeRef = another.typeRef;
        this.numberOfDimensions = another.numberOfDimensions;
    }

    public Reference getReference() {
        return this.reference;
    }

    public boolean isNullAllowed() {
        return this.isNullAllowed;
    }

    public int getNumberOfDimensions() {
        return this.numberOfDimensions;
    }

    public void setNumberOfDimensions(int numberOfDimensions) {
        this.numberOfDimensions = numberOfDimensions;
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
            return (this.isNullAllowed && context.isNull() || (this.isNull() && context.isNullAllowed));
        }
    }

    public void setArithmetic() {
        this.reference = Reference.ARTIHMETIC;
        this.isNullAllowed = false;
        this.numberOfDimensions = 0;
    }

    public void setBoolean() {
        this.reference = Reference.BOOLEAN;
        this.isNullAllowed = false;
        this.numberOfDimensions = 0;
    }

    public void setNull() {
        this.reference = Reference.NULL;
        this.isNullAllowed = true;
        this.numberOfDimensions = 0;
    }

    public void setType(TypeReference ref) {

        switch (ref.getName()) {
            case INT_NAME:
                if (ref.getNumberOfDimensions() == 0) {
                    this.reference = Reference.ARTIHMETIC;
                    this.isNullAllowed = false;
                    this.numberOfDimensions = 0;
                }
                else {
                    this.reference = Reference.TYPE;
                    this.isNullAllowed = true;
                    this.numberOfDimensions = ref.getNumberOfDimensions();
                    this.typeRef = ref;
                }
                break;
            case BOOLEAN_NAME:
                if (ref.getNumberOfDimensions() == 0) {
                    this.reference = Reference.BOOLEAN;
                    this.isNullAllowed = false;
                    this.numberOfDimensions = 0;
                }
                else {
                    this.reference = Reference.TYPE;
                    this.isNullAllowed = true;
                    this.numberOfDimensions = ref.getNumberOfDimensions();
                    this.typeRef = ref;
                }
                break;
            default:
                this.reference = Reference.TYPE;
                this.isNullAllowed = true;
                this.numberOfDimensions = ref.getNumberOfDimensions();
                this.typeRef = ref;
        }

    }

    public void reduceDimension() {
        this.numberOfDimensions = this.numberOfDimensions > 0 ? this.numberOfDimensions - 1 : 0;

        // Convert back to primitive type if dimension reaches zero
        if (this.numberOfDimensions == 0) {
            switch (this.typeRef.getName()) {
                case INT_NAME:
                    this.reference = Reference.ARTIHMETIC;
                    this.isNullAllowed = false;
                    break;
                case BOOLEAN_NAME:
                    this.reference = Reference.BOOLEAN;
                    this.isNullAllowed = false;
                    break;
                default:
                    // Nothing to change here
                    break;
            }
        }
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
