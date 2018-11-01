package edu.kit.minijava.parser;

import java.util.*;

import util.INodeVisitor;

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
    public void accept(INodeVisitor visitor) {
        visitor.visit(this);
    }
}
