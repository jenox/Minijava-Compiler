package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.lexer.*;

public final class ClassReference extends SimpleReference<ClassDeclaration> {
    public ClassReference(String name, TokenLocation location) {
        super(location);

        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    private final String name;

    public String getName() {
        return this.name;
    }
}
