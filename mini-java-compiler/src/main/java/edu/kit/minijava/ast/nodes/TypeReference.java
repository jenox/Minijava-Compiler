package edu.kit.minijava.ast.nodes;

import edu.kit.minijava.ast.references.*;

public interface TypeReference extends ASTNode {
    Reference<BasicTypeDeclaration> getBasicTypeReference();
    int getNumberOfDimensions();

    default boolean isVoid() {
        if (this.getBasicTypeReference().getDeclaration() == PrimitiveTypeDeclaration.VOID) {
            return this.getNumberOfDimensions() == 0;
        }
        else {
            return false;
        }
    }

    default boolean isArrayOfVoid() {
        if (this.getBasicTypeReference().getDeclaration() == PrimitiveTypeDeclaration.VOID) {
            return this.getNumberOfDimensions() >= 1;
        }
        else {
            return false;
        }
    }
}
