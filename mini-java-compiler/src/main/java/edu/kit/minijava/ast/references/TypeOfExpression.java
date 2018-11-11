package edu.kit.minijava.ast.references;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class TypeOfExpression {

    public TypeOfExpression() {
    }

    private boolean isResolved = false;
    private BasicTypeDeclaration declaration = null;
    private int numberOfDimensions = -1;
    private boolean isAssignable = false;

    public final boolean isResolved() {
        return this.isResolved;
    }

    public final boolean isAssignable() {
        return this.isAssignable;
    }

    public final Optional<BasicTypeDeclaration> getDeclaration() {
        assert this.isResolved;

        return Optional.ofNullable(this.declaration);
    }

    public final int getNumberOfDimensions() {
        assert this.isResolved;

        return this.numberOfDimensions;
    }

    public final void resolveToNull() {
        assert !this.isResolved;

        this.declaration = null;
        this.numberOfDimensions = -1;
        this.isAssignable = false;
        this.isResolved = true;
    }

    public final void resolveTo(BasicTypeDeclaration declaration, int numberOfDimensions, boolean isAssignable) {
        assert !this.isResolved;
        assert declaration != null;
        assert numberOfDimensions >= 0;

        this.declaration = declaration;
        this.numberOfDimensions = numberOfDimensions;
        this.isAssignable = isAssignable;
        this.isResolved = true;
    }

    public final void resolveTo(BasicTypeDeclaration declaration, boolean isAssignable) {
        this.resolveTo(declaration, 0, isAssignable);
    }

    public final void resolveTo(TypeReference reference, boolean isAssignable) {
        this.resolveTo(reference.getDeclaration(), reference.getNumberOfDimensions(), isAssignable);
    }

    public final void resolveTo(BasicTypeReference reference, int numberOfDimensions, boolean isAssignable) {
        this.resolveTo(reference.getDeclaration(), numberOfDimensions, isAssignable);
    }

    public final void resolveTo(ClassReference reference, boolean isAssignable) {
        this.resolveTo(reference.getDeclaration(), 0, isAssignable);
    }

    public final void resolveTo(TypeOfExpression type, boolean isAssignable) {
        if (type.getDeclaration().isPresent()) {
            this.resolveTo(type.getDeclaration().get(), type.getNumberOfDimensions(), isAssignable);
        }
        else {
            this.resolveToNull();
        }
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

    public final boolean isNull() {
        assert this.isResolved;

        return this.declaration == null;
    }

    public final boolean isVoid() {
        assert this.isResolved;

        return this.declaration == PrimitiveTypeDeclaration.VOID && this.numberOfDimensions == 0;
    }

    public final boolean isBoolean() {
        assert this.isResolved;

        return this.declaration == PrimitiveTypeDeclaration.BOOLEAN && this.numberOfDimensions == 0;
    }

    public final boolean isInteger() {
        assert this.isResolved;

        return this.declaration == PrimitiveTypeDeclaration.INTEGER && this.numberOfDimensions == 0;
    }

    public final boolean isObjectOrNull() {
        assert this.isResolved;

        return this.declaration == null || this.declaration instanceof ClassDeclaration && this.numberOfDimensions == 0;
    }

    public final void resolveToVoid() {
        this.resolveTo(PrimitiveTypeDeclaration.VOID, 0, false);
    }

    public final void resolveToBoolean() {
        this.resolveTo(PrimitiveTypeDeclaration.BOOLEAN, 0, false);
    }

    public final void resolveToInteger() {
        this.resolveTo(PrimitiveTypeDeclaration.INTEGER, 0, false);
    }

    public boolean isCompatibleWith(TypeReference reference) {
        assert this.isResolved;

        // reference is array
        if (reference.getNumberOfDimensions() >= 1) {
            if (this.numberOfDimensions != reference.getNumberOfDimensions()) return false;

            if (this.declaration != null) {
                return this.declaration == reference.getDeclaration();
            }
            else {
                // TODO: is `null` a valid value for arrays?
                return false;
            }
        }

        // reference is class
        if (reference.getDeclaration() instanceof ClassDeclaration) {
            return this.isObjectOrNull();
        }

        // reference is non-class
        else {
            if (this.declaration != reference.getDeclaration()) return false;
            if (this.numberOfDimensions != 0) return false;

            return true;
        }
    }

    public boolean isCompatibleWith(TypeOfExpression type) {
        assert this.isResolved;

        if (this.isInteger() && type.isInteger()) return true;
        if (this.isBoolean() && type.isBoolean()) return true;
        if (this.isObjectOrNull() && type.isObjectOrNull()) return true;

        // TODO: can we assign/compare arrays?
        return false;
    }
}
