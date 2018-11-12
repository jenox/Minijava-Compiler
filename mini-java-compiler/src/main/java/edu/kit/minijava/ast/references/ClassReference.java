package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class ClassReference extends ExplicitReference<ClassDeclaration> {
    public ClassReference(String name, TokenLocation location) {
        super(name, location);
    }
}
