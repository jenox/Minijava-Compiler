package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

public final class ClassReference extends AbstractReference<ClassDeclaration> {
    public ClassReference(String name) {
        if (name == null) throw new IllegalArgumentException();

        this.name = name;
    }

    private final String name;

    public String getName() {
        return this.name;
    }
}
