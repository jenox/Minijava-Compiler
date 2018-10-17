package edu.kit.minijava.lexer;

public class Token {
    public Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    public final TokenType type;
    public final String text;

    @Override public String toString() {
        switch (this.type) {
            case IDENTIFIER:
            case INTEGER_LITERAL:
                return this.type.toString() + "(" + this.text + ")";
            default:
                return this.type.toString();
        }
    }
}
