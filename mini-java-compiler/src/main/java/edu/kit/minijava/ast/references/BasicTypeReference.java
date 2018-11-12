package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class BasicTypeReference extends SimpleReference<BasicTypeDeclaration> {
    public BasicTypeReference(String name, TokenLocation location) {
        super(name, location);
    }


    // MARK: - Convenience Methods

    public final boolean isVoid() {
        return this.getDeclaration() == PrimitiveTypeDeclaration.VOID;
    }
}
