package edu.kit.minijava.ast2;

import java.util.*;

public final class Program {
    public Program(List<ClassDeclaration> classes) {
        if (classes == null) throw new IllegalArgumentException();

        this.classes = Collections.unmodifiableList(classes);
    }

    public final List<ClassDeclaration> classes;
}
