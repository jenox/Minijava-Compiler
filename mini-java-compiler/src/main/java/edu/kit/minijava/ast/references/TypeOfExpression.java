package edu.kit.minijava.ast.references;

import java.util.Optional;

import edu.kit.minijava.ast.nodes.*;

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

    public final Optional<BasicTypeDeclaration> getDeclaration() {
        assert this.isResolved;

        return Optional.ofNullable(this.declaration);
    }

    public final int getNumberOfDimensions() {
        assert this.isResolved;

        return this.numberOfDimensions;
    }

    public final boolean isAssignable() {
        return this.isAssignable;
    }

    // MARK: - Resolution

    public final void resolveToNull() {
        assert !this.isResolved;

        this.declaration = null;
        this.numberOfDimensions = -1;
        this.isAssignable = false;
        this.isResolved = true;
    }

    public final void resolveToBoolean() {
        this.resolveToArrayOf(PrimitiveTypeDeclaration.BOOLEAN, 0, false);
    }

    public final void resolveToInteger() {
        this.resolveToArrayOf(PrimitiveTypeDeclaration.INTEGER, 0, false);
    }

    public final void resolveToArrayOf(BasicTypeDeclaration declaration, int numberOfDimensions, boolean isAssignable) {
        assert !this.isResolved;
        assert declaration != null;
        assert numberOfDimensions >= 0;

        this.declaration = declaration;
        this.numberOfDimensions = numberOfDimensions;
        this.isAssignable = isAssignable;
        this.isResolved = true;
    }

    public final void resolveToInstanceOfClass(ClassDeclaration declaration, boolean isAssignable) {
        this.resolveToArrayOf(declaration, 0, isAssignable);
    }

    public final void resolveToTypeReference(TypeReference reference, boolean isAssignable) {
        BasicTypeDeclaration declaration = reference.getBasicTypeReference().getDeclaration();

        this.resolveToArrayOf(declaration, reference.getNumberOfDimensions(), isAssignable);
    }

    public final void resolveToVariableDeclaration(VariableDeclaration declaration) {
        this.resolveToTypeReference(declaration.getType(), !declaration.isFinal());
    }

    public final void resolveToTypeOfExpression(TypeOfExpression type, boolean isAssignable) {
        if (type.getDeclaration().isPresent()) {
            this.resolveToArrayOf(type.getDeclaration().get(), type.getNumberOfDimensions(), isAssignable);
        }
        else {
            this.resolveToNull();
        }
    }


    // MARK: - Convenience Queries

    public final boolean isBoolean() {
        assert this.isResolved;

        return this.declaration == PrimitiveTypeDeclaration.BOOLEAN && this.numberOfDimensions == 0;
    }

    public final boolean isInteger() {
        assert this.isResolved;

        return this.declaration == PrimitiveTypeDeclaration.INTEGER && this.numberOfDimensions == 0;
    }

    public final boolean isNull() {
        assert this.isResolved;

        return this.declaration == null;
    }

    public final boolean isObjectOrNull() {
        assert this.isResolved;

        return this.declaration == null || this.declaration instanceof ClassDeclaration && this.numberOfDimensions == 0;
    }

    public final boolean isArrayType() {
        assert this.isResolved;

        return this.numberOfDimensions > 0;
    }


    // MARK: - Other

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
                return this.declaration.getName() + ", " + this.numberOfDimensions + " dimensions";
            }
            else {
                return this.declaration.getName();
            }
        }
    }
}
