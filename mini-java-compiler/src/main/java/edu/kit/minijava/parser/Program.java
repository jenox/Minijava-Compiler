package edu.kit.minijava.parser;

import java.util.*;

public final class Program {
    public Program(List<ClassDeclaration> classDeclarations) {
        this.classDeclarations = Collections.unmodifiableList(classDeclarations);
    }

    public final List<ClassDeclaration> classDeclarations;
}
