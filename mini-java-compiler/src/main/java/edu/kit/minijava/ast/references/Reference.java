package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public abstract class Reference {
    Reference(String name, TokenLocation location) {
        this.name = name;
        this.location = location;
    }

    private final String name;
    private final TokenLocation location;

    public final String getName() {
        return this.name;
    }

    public final TokenLocation getLocation() {
        return this.location;
    }

    public abstract boolean isResolved();

    public abstract Declaration getDeclaration();

    @Override
    public String toString() {
        return this.name + " at " + this.location;
    }
}
