package edu.kit.minijava.ast2.nodes;

public enum UnaryOperationType {
    LOGICAL_NEGATION("!"),
    NUMERIC_NEGATION("-");

    UnaryOperationType(String operatorSymbol) {
        this.operatorSymbol = operatorSymbol;
    }

    public final String operatorSymbol;
}
