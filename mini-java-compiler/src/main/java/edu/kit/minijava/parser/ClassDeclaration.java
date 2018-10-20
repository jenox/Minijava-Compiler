package edu.kit.minijava.parser;

import java.util.*;

public final class ClassDeclaration {
    public ClassDeclaration(String className, List<ClassMember> members) {
        this.className = className;
        this.members = Collections.unmodifiableList(members);
    }

    public final String className;
    public final List<ClassMember> members;
}
