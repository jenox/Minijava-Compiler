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

    private final TokenType type;
    private final String text;
    private final TokenLocation location;

    public TokenType getType() {
        return this.type;
    }

    public String getText() {
        return this.text;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

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
