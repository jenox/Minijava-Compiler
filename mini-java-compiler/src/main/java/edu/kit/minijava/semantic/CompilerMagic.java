package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

class CompilerMagic {
    static final ClassDeclaration SYSTEM;
    static final ClassDeclaration SYSTEM_OUT;
    static final ClassDeclaration SYSTEM_IN;

    static final FieldDeclaration OUT;
    static final FieldDeclaration IN;

    static final MethodDeclaration PRINTLN;
    static final MethodDeclaration WRITE;
    static final MethodDeclaration FLUSH;
    static final MethodDeclaration READ;

    static final VariableDeclaration SYSTEM_VARIABLE;

    static <T> List<T> list(T... elements) {
        return Arrays.asList(elements);
    }

    static {
        TypeReference voidReference = new ImplicitTypeReference(PrimitiveTypeDeclaration.VOID, 0);
        TypeReference integerReference = new ImplicitTypeReference(PrimitiveTypeDeclaration.INTEGER, 0);
        ParameterDeclaration integerParameter = new ParameterDeclaration(integerReference, null, null);

        PRINTLN = new MethodDeclaration(voidReference, "println", list(integerParameter), null, null);
        WRITE = new MethodDeclaration(voidReference, "write", list(integerParameter), null, null);
        FLUSH = new MethodDeclaration(voidReference, "flush", list(), null, null);
        READ = new MethodDeclaration(integerReference, "read", list(), null, null);

        SYSTEM_OUT = new ClassDeclaration("#SystemOut", list(), list(FLUSH, WRITE, PRINTLN), list(), null);
        SYSTEM_IN = new ClassDeclaration("#SystemIn", list(), list(READ), list(), null);

        OUT = new FieldDeclaration(new ImplicitTypeReference(SYSTEM_OUT, 0), "out", null);
        IN = new FieldDeclaration(new ImplicitTypeReference(SYSTEM_IN, 0), "in", null);

        SYSTEM = new ClassDeclaration("#System", list(), list(), list(OUT, IN), null);

        SYSTEM_VARIABLE = new VariableDeclaration() {
            private TypeReference reference = new ImplicitTypeReference(SYSTEM, 0);

            @Override
            public String getName() {
                return "System";
            }

            @Override
            public TypeReference getType() {
                return this.reference;
            }

            @Override
            public boolean canBeAccessed() {
                return true;
            }

            @Override
            public boolean canBeShadowedByVariableDeclarationInNestedScope() {
                return true;
            }

            @Override
            public <T> void accept(ASTVisitor<T> visitor) {}

            @Override
            public <T> void accept(ASTVisitor<T> visitor, T context) {}
        };
    }
}
