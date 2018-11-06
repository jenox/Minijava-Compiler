package edu.kit.minijava.ast.nodes;

public enum UnaryOperationType {
    LOGICAL_NEGATION("!"),
    NUMERIC_NEGATION("-");

    UnaryOperationType(String operatorSymbol) {
        this.operatorSymbol = operatorSymbol;
    }

    private final String operatorSymbol;

    public String getOperatorSymbol() {
        return this.operatorSymbol;
    }
}
