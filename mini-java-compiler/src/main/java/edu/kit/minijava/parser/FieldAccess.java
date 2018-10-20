package edu.kit.minijava.parser;

public final class FieldAccess extends PostfixOperation {
    public FieldAccess(String fieldName) {
        this.fieldName = fieldName;
    }

    public final String fieldName;
}
