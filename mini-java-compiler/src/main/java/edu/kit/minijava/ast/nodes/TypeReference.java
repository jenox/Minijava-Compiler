package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

public interface TypeReference extends ASTNode {
    Reference<BasicTypeDeclaration> getBasicTypeReference();
    int getNumberOfDimensions();
    boolean isVoid();
}
