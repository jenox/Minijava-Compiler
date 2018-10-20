package edu.kit.minijava.parser;

public final class Type {
    public Type(BasicType basicType, int numberOfDimensions) {
        this.basicType = basicType;
        this.numberOfDimensions = numberOfDimensions;
    }

    public final BasicType basicType;
    public final int numberOfDimensions;
}
