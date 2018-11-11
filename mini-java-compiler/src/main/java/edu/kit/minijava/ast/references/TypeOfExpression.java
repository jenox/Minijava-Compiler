package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class TypeOfExpression {

    public TypeOfExpression() {
    }

    private boolean isResolved = false;
    private BasicTypeDeclaration declaration = null;
    private int numberOfDimensions = -1;

    public final boolean isResolved() {
        return this.isResolved;
    }

    public final Optional<BasicTypeDeclaration> getDeclaration() {
        assert this.isResolved;

        return Optional.ofNullable(this.declaration);
    }

    public final int getNumberOfDimensions() {
        assert this.isResolved;

        return this.numberOfDimensions;
    }

    public final void resolveTo(BasicTypeDeclaration declaration, int numberOfDimensions) {
        assert !this.isResolved;
        assert declaration != null;
        assert numberOfDimensions >= 0;

        this.declaration = declaration;
        this.isResolved = true;
        this.numberOfDimensions = numberOfDimensions;
    }

    public final void resolveTo(BasicTypeDeclaration declaration) {
        this.resolveTo(declaration, 0);
    }

    public final void resolveToNull() {
        assert !this.isResolved;

        this.declaration = null;
        this.isResolved = true;
        this.numberOfDimensions = -1;
    }

    @Override
    public String toString() {
        if (!this.isResolved) {
            return "-";
        }

        if (this.declaration == null) {
            return "null";
        }
        else {
            if (this.numberOfDimensions >= 1) {
                return this.declaration + ", " + this.numberOfDimensions + " dimensions";
            }
            else {
                return this.declaration.toString();
            }
        }
    }
}
