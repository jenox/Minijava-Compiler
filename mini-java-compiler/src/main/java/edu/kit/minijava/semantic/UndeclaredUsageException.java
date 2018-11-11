package edu.kit.minijava.semantic;

import edu.kit.minijava.lexer.*;

public class UndeclaredUsageException extends SemanticAnalysisException {

    private final String identifier;
    private final TokenLocation tokenLocation;

    public UndeclaredUsageException(String identifier, TokenLocation location) {

        if (identifier == null) throw new IllegalArgumentException();

        this.identifier = identifier;
        this.tokenLocation = location; // nullable
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public TokenLocation getTokenLocation() {
        return this.tokenLocation;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();

        builder.append("Usage of undeclared token ");
        builder.append(this.getIdentifier());

        if (this.getTokenLocation() != null) {
            builder.append(" at ");
            builder.append(this.getTokenLocation());
        }
        builder.append(".");

        return builder.toString();
    }
}
