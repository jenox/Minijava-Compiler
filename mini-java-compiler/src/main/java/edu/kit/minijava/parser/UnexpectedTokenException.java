package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;

public class UnexpectedTokenException extends ParserException {
    public UnexpectedTokenException(Token actualToken, String context, TokenType... expectedTypes) {
        if (context == null) throw new IllegalArgumentException();
        for (TokenType type : expectedTypes) {
            if (type == null) throw new IllegalArgumentException();
        }

        this.actualToken = actualToken;
        this.context = context;
        this.expectedTypes = expectedTypes;
    }

    public final Token actualToken; // nullable
    public final String context;
    public final TokenType[] expectedTypes;

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();

        if (this.actualToken != null) {
            builder.append("Unexpected token ");
            builder.append(this.actualToken);
            builder.append(" at ");
            builder.append(this.actualToken.location);
        } else {
            builder.append("Unexpected EOF");
        }

        builder.append(" while parsing ");
        builder.append(this.context);
        builder.append(".");

        if (this.expectedTypes.length > 0) {
            builder.append(" Expected ");

            if (this.expectedTypes.length == 1) {
                builder.append(this.expectedTypes[0]);
            } else if (this.expectedTypes.length == 2) {
                builder.append(this.expectedTypes[0]);
                builder.append(" or ");
                builder.append(this.expectedTypes[1]);
            } else {
                for (int index = 0; index < this.expectedTypes.length - 2; index += 1) {
                    builder.append(this.expectedTypes[index]);
                    builder.append(", ");
                }

                builder.append(this.expectedTypes[this.expectedTypes.length - 2]);
                builder.append(", or ");
                builder.append(this.expectedTypes[this.expectedTypes.length - 1]);
            }

            builder.append(".");
        }

        return builder.toString();
    }
}
