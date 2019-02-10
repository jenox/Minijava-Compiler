package edu.kit.minijava.semantic;

import edu.kit.minijava.lexer.TokenLocation;

public class SemanticException extends Exception {
    public SemanticException() {
        this(null, null, null);
    }

    public SemanticException(String message) {
        this(message, null, null);
    }

    public SemanticException(String message, String context) {
        this(message, context, null);
    }

    public SemanticException(String message, String context, TokenLocation location) {
        this.message = message;
        this.context = context;
        this.location = location;
    }

    private String message;
    private String context;
    private TokenLocation location;

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        if (this.message == null) {
            return "Semantic error.";
        }

        builder.append(this.message);

        if (this.location != null) {
            builder.append(" at ");
            builder.append(this.location);
        }

        if (this.context != null) {
            builder.append(" (error in ");
            builder.append(this.context);
            builder.append(")");
        }
        builder.append(".");

        return builder.toString();
    }
}
