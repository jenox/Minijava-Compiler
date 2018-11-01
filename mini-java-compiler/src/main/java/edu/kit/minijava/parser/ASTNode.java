package edu.kit.minijava.parser;

import util.INodeVisitor;

public abstract class ASTNode {
    
    public abstract void accept(INodeVisitor visitor);
}
