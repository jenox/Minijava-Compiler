package edu.kit.minijava.ast;

public abstract class ClassMember extends ASTNode {

    /**
     *
     * @return name in order to sort class members alphabetically
     */
    public abstract String getName();

    /**
     * We need to know whether or not this classmember is a method, in order
     * to sort methods before fields
     * @return true iff this is a method
     */
    public abstract boolean isMethod();
}
