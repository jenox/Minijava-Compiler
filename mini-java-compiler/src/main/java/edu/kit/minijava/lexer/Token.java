package edu.kit.minijava.lexer;

public class Token {
    public Token(TokenType type, String text, TokenLocation location) {
        if (type == null) throw new IllegalArgumentException();
        if (text == null) throw new IllegalArgumentException();
        if (text.isEmpty()) throw new IllegalArgumentException();
        if (location == null) throw new IllegalArgumentException();

        this.type = type;
        this.text = text.intern();
        this.location = location;
    }

    public final TokenType type;
    public final String text;
    public final TokenLocation location;

    @Override
    public String toString() {
        switch (this.type) {
            case IDENTIFIER:
            case INTEGER_LITERAL:
                return this.type.toString() + "(" + this.text + ")";
            default:
                return this.type.toString();
        }
    }
}
