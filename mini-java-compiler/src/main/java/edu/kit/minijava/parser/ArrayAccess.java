package edu.kit.minijava.parser;

public final class ArrayAccess extends PostfixOperation {
    public ArrayAccess(Expression index) {
        this.index = index;
    }

    public final Expression index;
}
