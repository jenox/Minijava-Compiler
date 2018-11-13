package edu.kit.minijava.semantic;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.ast.references.*;
import edu.kit.minijava.ast.nodes.*;

import org.junit.*;

import java.util.*;

public class Tests {
    public Tests() {
        TokenLocation location = new TokenLocation(0,0);
        List<FieldDeclaration> fields = Collections.emptyList();
        List<MethodDeclaration> methods = Collections.emptyList();
        List<MainMethodDeclaration> mainMethods = Collections.emptyList();

        ClassDeclaration classX = new ClassDeclaration("X", mainMethods, methods, fields, location);
        ClassDeclaration classY = new ClassDeclaration("Y", mainMethods, methods, fields, location);

        this.classXReference = new ImplicitTypeReference(classX, 0);
        this.classXReference1D = new ImplicitTypeReference(classX, 1);
        this.classXReference2D = new ImplicitTypeReference(classX, 2);
        this.classYReference = new ImplicitTypeReference(classY, 0);
        this.classYReference1D = new ImplicitTypeReference(classY, 1);
        this.classYReference2D = new ImplicitTypeReference(classY, 2);

        this.nullType.resolveToNull();
        this.voidType.resolveToArrayOf(PrimitiveTypeDeclaration.VOID, 0, true);
        this.voidType1D.resolveToArrayOf(PrimitiveTypeDeclaration.VOID, 1, true);
        this.voidType2D.resolveToArrayOf(PrimitiveTypeDeclaration.VOID, 2, true);
        this.integerType.resolveToArrayOf(PrimitiveTypeDeclaration.INTEGER, 0, true);
        this.integerType1D.resolveToArrayOf(PrimitiveTypeDeclaration.INTEGER, 1, true);
        this.integerType2D.resolveToArrayOf(PrimitiveTypeDeclaration.INTEGER, 2, true);
        this.booleanType.resolveToArrayOf(PrimitiveTypeDeclaration.BOOLEAN, 0, true);
        this.booleanType1D.resolveToArrayOf(PrimitiveTypeDeclaration.BOOLEAN, 1, true);
        this.booleanType2D.resolveToArrayOf(PrimitiveTypeDeclaration.BOOLEAN, 2, true);
        this.classXType.resolveToArrayOf(classX, 0, true);
        this.classXType1D.resolveToArrayOf(classX, 1, true);
        this.classXType2D.resolveToArrayOf(classX, 2, true);
        this.classYType.resolveToArrayOf(classY, 0, true);
        this.classYType1D.resolveToArrayOf(classY, 1, true);
        this.classYType2D.resolveToArrayOf(classY, 2, true);

        this.allTypesOfExpressions = Arrays.asList(
                this.nullType,
                this.voidType, this.voidType1D, this.voidType2D,
                this.integerType, this.integerType1D, this.integerType2D,
                this.booleanType, this.booleanType1D, this.booleanType2D,
                this.classXType, this.classXType1D, this.classXType2D,
                this.classYType, this.classYType1D, this.classYType2D
        );
    }

    private final TypeReference voidReference = new ImplicitTypeReference(PrimitiveTypeDeclaration.VOID, 0);
    private final TypeReference voidReference1D = new ImplicitTypeReference(PrimitiveTypeDeclaration.VOID, 1);
    private final TypeReference voidReference2D = new ImplicitTypeReference(PrimitiveTypeDeclaration.VOID, 2);
    private final TypeReference integerReference = new ImplicitTypeReference(PrimitiveTypeDeclaration.INTEGER, 0);
    private final TypeReference integerReference1D = new ImplicitTypeReference(PrimitiveTypeDeclaration.INTEGER, 1);
    private final TypeReference integerReference2D = new ImplicitTypeReference(PrimitiveTypeDeclaration.INTEGER, 2);
    private final TypeReference booleanReference = new ImplicitTypeReference(PrimitiveTypeDeclaration.BOOLEAN, 0);
    private final TypeReference booleanReference1D = new ImplicitTypeReference(PrimitiveTypeDeclaration.BOOLEAN, 1);
    private final TypeReference booleanReference2D = new ImplicitTypeReference(PrimitiveTypeDeclaration.BOOLEAN, 2);
    private final TypeReference classXReference;
    private final TypeReference classXReference1D;
    private final TypeReference classXReference2D;
    private final TypeReference classYReference;
    private final TypeReference classYReference1D;
    private final TypeReference classYReference2D;

    private final TypeOfExpression nullType = new TypeOfExpression();
    private final TypeOfExpression voidType = new TypeOfExpression();
    private final TypeOfExpression voidType1D = new TypeOfExpression();
    private final TypeOfExpression voidType2D = new TypeOfExpression();
    private final TypeOfExpression integerType = new TypeOfExpression();
    private final TypeOfExpression integerType1D = new TypeOfExpression();
    private final TypeOfExpression integerType2D = new TypeOfExpression();
    private final TypeOfExpression booleanType = new TypeOfExpression();
    private final TypeOfExpression booleanType1D = new TypeOfExpression();
    private final TypeOfExpression booleanType2D = new TypeOfExpression();
    private final TypeOfExpression classXType = new TypeOfExpression();
    private final TypeOfExpression classXType1D = new TypeOfExpression();
    private final TypeOfExpression classXType2D = new TypeOfExpression();
    private final TypeOfExpression classYType = new TypeOfExpression();
    private final TypeOfExpression classYType1D = new TypeOfExpression();
    private final TypeOfExpression classYType2D = new TypeOfExpression();
    private final List<TypeOfExpression> allTypesOfExpressions;


    // MARK: - Type-Reference Compatibility

    @Test public void testVoidReferencesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.voidReference, this.voidType);
        this.assertCanBeAssignedOnlyBy(this.voidReference1D, this.voidType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.voidReference2D, this.voidType2D, this.nullType);
    }

    @Test public void testIntegerReferencesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.integerReference, this.integerType);
        this.assertCanBeAssignedOnlyBy(this.integerReference1D, this.integerType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.integerReference2D, this.integerType2D, this.nullType);
    }

    @Test public void testBooleanReferencesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.booleanReference, this.booleanType);
        this.assertCanBeAssignedOnlyBy(this.booleanReference1D, this.booleanType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.booleanReference2D, this.booleanType2D, this.nullType);
    }

    @Test public void testClassXReferencesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.classXReference, this.classXType, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classXReference1D, this.classXType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classXReference2D, this.classXType2D, this.nullType);
    }

    @Test public void testClassYReferencesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.classYReference, this.classYType, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classYReference1D, this.classYType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classYReference2D, this.classYType2D, this.nullType);
    }

    private void assertCanBeAssignedOnlyBy(TypeReference left, TypeOfExpression... validOptionsArray) {
        List<TypeOfExpression> validOptions = Arrays.asList(validOptionsArray);
        List<TypeOfExpression> invalidOptions = new ArrayList<>(this.allTypesOfExpressions);
        invalidOptions.removeAll(validOptions);

        for (TypeOfExpression validOption : validOptions) {
            this.assertCanBeAssignedBy(true, left, validOption);
        }

        for (TypeOfExpression invalidOption : invalidOptions) {
            this.assertCanBeAssignedBy(false, left, invalidOption);
        }
    }

    private void assertCanBeAssignedBy(boolean expected, TypeReference left, TypeOfExpression right) {
        String message = "Can " + left + " be assigned by " + right + "?";
        Assert.assertEquals(message, expected, SemanticAnalysisVisitorBase.canAssignTypeOfExpressionToTypeReference(right, left));
    }



    // MARK: - Type-Type Compatibility

    @Test public void testNullTypeAssignment() {
        this.assertCanBeAssignedOnlyBy(this.nullType);
    }

    @Test public void testVoidTypesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.voidType, this.voidType);
        this.assertCanBeAssignedOnlyBy(this.voidType1D, this.voidType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.voidType2D, this.voidType2D, this.nullType);
    }

    @Test public void testIntegerTypesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.integerType, this.integerType);
        this.assertCanBeAssignedOnlyBy(this.integerType1D, this.integerType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.integerType2D, this.integerType2D, this.nullType);
    }

    @Test public void testBooleanTypesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.booleanType, this.booleanType);
        this.assertCanBeAssignedOnlyBy(this.booleanType1D, this.booleanType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.booleanType2D, this.booleanType2D, this.nullType);
    }

    @Test public void testClassXTypesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.classXType, this.classXType, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classXType1D, this.classXType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classXType2D, this.classXType2D, this.nullType);
    }

    @Test public void testClassYTypesAssignment() {
        this.assertCanBeAssignedOnlyBy(this.classYType, this.classYType, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classYType1D, this.classYType1D, this.nullType);
        this.assertCanBeAssignedOnlyBy(this.classYType2D, this.classYType2D, this.nullType);
    }

    private void assertCanBeAssignedOnlyBy(TypeOfExpression left, TypeOfExpression... validOptionsArray) {
        List<TypeOfExpression> validOptions = Arrays.asList(validOptionsArray);
        List<TypeOfExpression> invalidOptions = new ArrayList<>(this.allTypesOfExpressions);
        invalidOptions.removeAll(validOptions);

        for (TypeOfExpression validOption : validOptions) {
            this.assertCanBeAssignedBy(true, left, validOption);
        }

        for (TypeOfExpression invalidOption : invalidOptions) {
            this.assertCanBeAssignedBy(false, left, invalidOption);
        }
    }

    private void assertCanBeAssignedBy(boolean expected, TypeOfExpression left, TypeOfExpression right) {
        String message = "Can " + left + " be assigned by " + right + "?";
        Assert.assertEquals(message, expected, SemanticAnalysisVisitorBase.canAssignTypeOfExpressionToTypeOfExpression(right, left));
    }


    // MARK: - Equality Checks

    @Test public void testCanCheckForEqualityCommutativity() {
        for (TypeOfExpression left : this.allTypesOfExpressions) {
            for (TypeOfExpression right : this.allTypesOfExpressions) {
                boolean forward = SemanticAnalysisVisitorBase.canCheckForEqualityWithTypesOfExpressions(left, right);
                boolean backward = SemanticAnalysisVisitorBase.canCheckForEqualityWithTypesOfExpressions(right, left);

                Assert.assertEquals(forward, backward);
            }
        }
    }

    @Test public void testCanCheckNullTypeForEquality() {
        this.assertCanCompareExpressionsOnlyTo(this.nullType,
                this.nullType,
                this.voidType1D, this.voidType2D,
                this.integerType1D, this.integerType2D,
                this.booleanType1D, this.booleanType2D,
                this.classXType, this.classXType1D, this.classXType2D,
                this.classYType, this.classYType1D, this.classYType2D
        );
    }

    @Test public void testCanCheckVoidTypesForEquality() {
        this.assertCanCompareExpressionsOnlyTo(this.voidType); // IMPORTANT! Can't compare void to void!
        this.assertCanCompareExpressionsOnlyTo(this.voidType1D, this.nullType, this.voidType1D);
        this.assertCanCompareExpressionsOnlyTo(this.voidType2D, this.nullType, this.voidType2D);
    }

    @Test public void testCanCheckIntegerTypesForEquality() {
        this.assertCanCompareExpressionsOnlyTo(this.integerType, this.integerType);
        this.assertCanCompareExpressionsOnlyTo(this.integerType1D, this.nullType, this.integerType1D);
        this.assertCanCompareExpressionsOnlyTo(this.integerType2D, this.nullType, this.integerType2D);
    }

    @Test public void testCanCheckBooleanTypesForEquality() {
        this.assertCanCompareExpressionsOnlyTo(this.booleanType, this.booleanType);
        this.assertCanCompareExpressionsOnlyTo(this.booleanType1D, this.nullType, this.booleanType1D);
        this.assertCanCompareExpressionsOnlyTo(this.booleanType2D, this.nullType, this.booleanType2D);
    }

    @Test public void testCanCheckClassXTypesForEquality() {
        this.assertCanCompareExpressionsOnlyTo(this.classXType, this.nullType, this.classXType);
        this.assertCanCompareExpressionsOnlyTo(this.classXType1D, this.nullType, this.classXType1D);
        this.assertCanCompareExpressionsOnlyTo(this.classXType2D, this.nullType, this.classXType2D);
    }

    @Test public void testCanCheckClassYTypesForEquality() {
        this.assertCanCompareExpressionsOnlyTo(this.classYType, this.nullType, this.classYType);
        this.assertCanCompareExpressionsOnlyTo(this.classYType1D, this.nullType, this.classYType1D);
        this.assertCanCompareExpressionsOnlyTo(this.classYType2D, this.nullType, this.classYType2D);
    }

    private void assertCanCompareExpressionsOnlyTo(TypeOfExpression left, TypeOfExpression... validOptionsArray) {
        List<TypeOfExpression> validOptions = Arrays.asList(validOptionsArray);
        List<TypeOfExpression> invalidOptions = new ArrayList<>(this.allTypesOfExpressions);
        invalidOptions.removeAll(validOptions);

        for (TypeOfExpression validOption : validOptions) {
            this.assertCanCheckForEquality(true, left, validOption);
        }

        for (TypeOfExpression invalidOption : invalidOptions) {
            this.assertCanCheckForEquality(false, left, invalidOption);
        }
    }

    private void assertCanCheckForEquality(boolean expected, TypeOfExpression left, TypeOfExpression right) {
        String message = "Can check for equality with " + left + " and " + right + "?";
        Assert.assertEquals(message, expected, SemanticAnalysisVisitorBase.canCheckForEqualityWithTypesOfExpressions(left, right));
    }
}
