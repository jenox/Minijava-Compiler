package edu.kit.minijava.ast;

import java.util.Collections;
import java.util.List;

public final class Program extends ASTNode {
    public Program(List<ClassDeclaration> classDeclarations) {
        if (classDeclarations == null) throw new IllegalArgumentException();

        this.classDeclarations = Collections.unmodifiableList(classDeclarations);
    }

    public final List<ClassDeclaration> classDeclarations;

    @Override
    public String toString() {
        return "Program(" + this.classDeclarations + ")";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
