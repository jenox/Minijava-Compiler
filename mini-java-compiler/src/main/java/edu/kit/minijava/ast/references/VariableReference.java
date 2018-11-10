package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class VariableReference extends SimpleReference<VariableDeclaration> {
    public VariableReference(String name, TokenLocation location) {
        super(name, location);
    }
}
