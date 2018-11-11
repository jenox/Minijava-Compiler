package edu.kit.minijava.ast.nodes;

public interface ASTNode {
    <T, ExceptionType extends Throwable> void accept(ASTVisitor<T, ExceptionType> visitor, T context) throws ExceptionType;
}
