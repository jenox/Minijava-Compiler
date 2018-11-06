package edu.kit.minijava.ast2.nodes;

import java.util.*;

public final class Program extends ASTNode {
    public Program(List<ClassDeclaration> classes) {
        if (classes == null) throw new IllegalArgumentException();

        this.classes = Collections.unmodifiableList(classes);
    }

    public final List<ClassDeclaration> classes;

    @Override
    public <T> void accept(ASTVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }
}
