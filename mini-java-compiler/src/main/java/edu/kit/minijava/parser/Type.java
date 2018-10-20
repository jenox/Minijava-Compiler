package edu.kit.minijava.parser;

public final class Type {
    public Type(BasicType basicType, int numberOfDimensions) {
        if (basicType == null) { throw new IllegalArgumentException(); }

        this.basicType = basicType;
        this.numberOfDimensions = numberOfDimensions;
    }

    public final BasicType basicType;
    public final int numberOfDimensions;

    @Override
    public String toString() {
        return "Type(" + this.basicType + ", " + this.numberOfDimensions + ")";
    }
}
