package edu.kit.minijava.semantic.typechecking;

import edu.kit.minijava.ast.references.TypeReference;

public class TypeContext {

    /**
     * some default types which are often used
     */
    public static final TypeContext BOOLEAN = new TypeContext(Reference.BOOLEAN);
    public static final TypeContext ARITHMETIC = new TypeContext(Reference.ARTIHMETIC);
    public static final TypeContext VOID = new TypeContext(Reference.VOID);

    public TypeContext(Reference reference) {
        this.reference = reference;
    }

    public TypeContext(String typeName) {
        this(typeName, 0);
    }

    public TypeContext(String typeName, int numberOfDimensions) {
        this.typeName = typeName;
        this.numberOfDimensions = numberOfDimensions;
        this.reference = Reference.TYPE;
    }

    private Reference reference;
    private boolean isNullAlowed;
    private String typeName;
    private int numberOfDimensions;

    public Reference getReference() {
        return this.reference;
    }

    public boolean isNullAlowed() {
        return this.isNullAlowed;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public int getNumberOfDimensions() {
        return this.numberOfDimensions;
    }

    public void setArithmetic() {
        this.reference = Reference.ARTIHMETIC;
        this.isNullAlowed = false;
        this.numberOfDimensions = 0;
        this.typeName = null;
    }

    public void setBoolean() {
        this.reference = Reference.BOOLEAN;
        this.isNullAlowed = false;
        this.numberOfDimensions = 0;
        this.typeName = null;
    }

    public void setType(TypeReference ref) {
        this.reference = Reference.TYPE;
        this.isNullAlowed = true;
        this.numberOfDimensions = ref.getNumberOfDimensions();
        this.typeName = ref.getName();
    }

}
