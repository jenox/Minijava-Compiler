package edu.kit.minijava.parser;

import util.INodeVisitor;

public final class Type extends ASTNode {
    public Type(BasicType basicType, int numberOfDimensions) {
        if (basicType == null) throw new IllegalArgumentException();

        this.basicType = basicType;
        this.numberOfDimensions = numberOfDimensions;
    }

    public final BasicType basicType;
    public final int numberOfDimensions;

    @Override
    public String toString() {
        return "Type(" + this.basicType + ", " + this.numberOfDimensions + ")";
    }
    
    @Override
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
