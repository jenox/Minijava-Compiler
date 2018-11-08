package edu.kit.minijava.ast.references;

import edu.kit.minijava.lexer.*;

public abstract class Reference {
    Reference(TokenLocation location) {
        this.location = location;
    }

    private final TokenLocation location;

    public TokenLocation getLocation() {
        return this.location;
    }
}
