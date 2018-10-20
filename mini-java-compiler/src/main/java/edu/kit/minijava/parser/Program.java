package edu.kit.minijava.parser;

import java.util.*;

public final class Program {
    public Program(List<ClassDeclaration> classDeclarations) {
        if (classDeclarations == null) throw new IllegalArgumentException();

        this.classDeclarations = Collections.unmodifiableList(classDeclarations);
    }

    public final List<ClassDeclaration> classDeclarations;

    @Override
    public String toString() {
        return "Program(" + this.classDeclarations + ")";
    }
}
