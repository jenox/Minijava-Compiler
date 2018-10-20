package edu.kit.minijava.parser;

import java.util.*;

public final class ClassDeclaration {
    public ClassDeclaration(String className, List<ClassMember> members) {
        if (className == null) { throw new IllegalArgumentException(); }
        if (members == null) { throw new IllegalArgumentException(); }

        this.className = className;
        this.members = Collections.unmodifiableList(members);
    }

    public final String className;
    public final List<ClassMember> members;

    @Override
    public String toString() {
        return "ClassDeclaration(" + this.className + ", " + this.members + ")";
    }
}
