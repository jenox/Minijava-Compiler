package edu.kit.minijava.ast;

public abstract class ASTNode {

    public abstract void accept(ASTVisitor visitor);
}
