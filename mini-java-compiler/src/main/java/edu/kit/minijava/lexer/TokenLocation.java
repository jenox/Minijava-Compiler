package edu.kit.minijava.lexer;

public final class TokenLocation {
    public TokenLocation(int row, int column) {
        this.row = row;
        this.column = column;
    }

    private final int row;
    private final int column;

    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }

    @Override
    public String toString() {
        return String.valueOf(this.row + 1) + ":" + String.valueOf(this.column + 1);
    }
}
