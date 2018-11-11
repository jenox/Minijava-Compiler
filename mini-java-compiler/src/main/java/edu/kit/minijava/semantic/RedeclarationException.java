package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.Declaration;
import edu.kit.minijava.lexer.TokenLocation;

public class RedeclarationException extends SemanticAnalysisException {

    private final Declaration originalDeclaration;
    private final Declaration redeclaration;

    // TODO Extract this from the declarations themselves instead
    private final TokenLocation tokenLocation;

    public RedeclarationException(String identifier, Declaration original, Declaration redeclaration,
                                  TokenLocation location) {
        if (identifier == null) throw new IllegalArgumentException();

        // TODO Maybe don't allow this to be null also?
        // if (original == null) throw new IllegalArgumentException();
        if (redeclaration == null) throw new IllegalArgumentException();
//        if (location == null) throw new IllegalArgumentException();

        this.originalDeclaration = original;
        this.redeclaration = redeclaration;
        this.tokenLocation = location;
    }

    public RedeclarationException(Declaration redeclaration, TokenLocation location) {
        this(redeclaration.getName(), null, redeclaration, location);
    }

    public Declaration getOriginalDeclaration() {
        return this.originalDeclaration;
    }

    public Declaration getRedeclaration() {
        return this.redeclaration;
    }

    public TokenLocation getTokenLocation() {
        return this.tokenLocation;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();

        builder.append("Name ");
        builder.append(this.redeclaration.getName());
        builder.append(" has already been declared");

        if (this.getTokenLocation() != null) {
            builder.append(" at ");
            builder.append(this.getTokenLocation());
        }
        builder.append(".");

        return builder.toString();
    }

}
