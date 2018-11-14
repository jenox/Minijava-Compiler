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

    static {
        TypeReference voidReference = new ImplicitTypeReference(PrimitiveTypeDeclaration.VOID, 0);
        TypeReference integerReference = new ImplicitTypeReference(PrimitiveTypeDeclaration.INTEGER, 0);
        ParameterDeclaration integerParameter = new ParameterDeclaration(integerReference, null, null);

        PRINTLN = new MethodDeclaration(voidReference, "println", Collections.singletonList(integerParameter), null,
                null);
        WRITE = new MethodDeclaration(voidReference, "write", Collections.singletonList(integerParameter), null, null);
        FLUSH = new MethodDeclaration(voidReference, "flush", Collections.emptyList(), null, null);
        READ = new MethodDeclaration(integerReference, "read", Collections.emptyList(), null, null);

        SYSTEM_OUT = new ClassDeclaration("#SystemOut", Collections.emptyList(), Arrays.asList(FLUSH, WRITE, PRINTLN),
                Collections.emptyList(),null);
        SYSTEM_IN = new ClassDeclaration("#SystemIn", Collections.emptyList(), Collections.singletonList(READ),
                Collections.emptyList(),null);

        OUT = new FieldDeclaration(new ImplicitTypeReference(SYSTEM_OUT, 0), "out", null);
        IN = new FieldDeclaration(new ImplicitTypeReference(SYSTEM_IN, 0), "in", null);

        SYSTEM = new ClassDeclaration("#System", Collections.emptyList(), Collections.emptyList(),
                Arrays.asList(OUT, IN),null);

        SYSTEM_VARIABLE = new VariableDeclaration() {
            private final TypeReference reference = new ImplicitTypeReference(SYSTEM, 0);

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
