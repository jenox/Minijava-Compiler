package edu.kit.minijava.lexer;

public final class TokenLocation {
    public TokenLocation(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public final int row;
    public final int column;

    @Override
    public String toString() {
        return String.valueOf(this.row + 1) + ":" + String.valueOf(this.column + 1);
    }
}
