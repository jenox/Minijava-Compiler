package edu.kit.minijava.parser;

public final class NewArrayExpression extends Expression {
    public NewArrayExpression(BasicType type, Expression primaryDimension, int numberOfDimensions) {
        if (type == null) throw new IllegalArgumentException();
        if (primaryDimension == null) throw new IllegalArgumentException();

        this.type = type;
        this.primaryDimension = primaryDimension;
        this.numberOfDimensions = numberOfDimensions;
    }

    public final BasicType type;
    public final Expression primaryDimension;
    public final int numberOfDimensions;

    @Override
    public String toString() {
        return "NewArrayExpression(" + this.type + ", " + this.primaryDimension + ", " + this.numberOfDimensions + ")";
    }
}
