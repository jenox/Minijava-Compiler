package edu.kit.minijava.ast.nodes;

public enum BinaryOperationType {
    MULTIPLICATION("*"),
    DIVISION("/"),
    MODULO("%"),
    ADDITION("+"),
    SUBTRACTION("-"),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    EQUAL_TO("=="),
    NOT_EQUAL_TO("!="),
    LOGICAL_AND("&&"),
    LOGICAL_OR("||"),
    ASSIGNMENT("=");

    BinaryOperationType(String operatorSymbol) {
        this.operatorSymbol = operatorSymbol;
    }

    public final String operatorSymbol;
}
