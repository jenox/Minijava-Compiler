package edu.kit.minijava.parser;

public final class NewArrayExpression extends Expression {
    public NewArrayExpression(BasicType type, Expression primaryDimension, int numberOfDimensions) {
        this.type = type;
        this.primaryDimension = primaryDimension;
        this.numberOfDimensions = numberOfDimensions;
    }

    public final BasicType type;
    public final Expression primaryDimension;
    public final int numberOfDimensions;
}
