package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

import java.util.*;

abstract class SemanticAnalysisVisitorBase extends ASTVisitor<Void> {
    SemanticAnalysisVisitorBase() {
    }


    // MARK: - General State

    private int currentTraversalNumber = 0;
    private final Stack<ClassDeclaration> currentClassDeclarations = new Stack<>();
    private final Stack<SubroutineDeclaration> currentMethodDeclarations = new Stack<>();
    private final Map<String, VariableDeclaration> globalVariableDeclarations = new HashMap<>();

    boolean isCollectingClassDeclarations() {
        return this.currentTraversalNumber == 0;
    }

    void finishCollectingClassDeclarations() {
        assert this.isCollectingClassDeclarations();

        this.currentTraversalNumber += 1;
    }

    boolean isCollectingClassMemberDeclarations() {
        return this.currentTraversalNumber == 1;
    }

    void finishCollectingClassMemberDeclarations() {
        assert this.isCollectingClassMemberDeclarations();

        this.currentTraversalNumber += 1;
    }

    ClassDeclaration getCurrentClassDeclaration() {
        return this.currentClassDeclarations.peek();
    }

    void enterClassDeclaration(ClassDeclaration declaration) {
        this.currentClassDeclarations.push(declaration);
        this.symbolTable.enterNewScope();
    }

    void leaveCurrentClassDeclaration() {
        this.symbolTable.leaveCurrentScope();
        this.currentClassDeclarations.pop();
    }

    SubroutineDeclaration getCurrentMethodDeclaration() {
        return this.currentMethodDeclarations.peek();
    }

    void enterMethodDeclaration(SubroutineDeclaration declaration) {
        this.currentMethodDeclarations.push(declaration);
        this.symbolTable.enterNewScope();
    }

    void leaveCurrentMethodDeclaration() {
        this.symbolTable.leaveCurrentScope();
        this.currentMethodDeclarations.pop();
    }


    // MARK: - Program Entry Point

    private MainMethodDeclaration entryPoint = null;

    Optional<MainMethodDeclaration> getEntryPoint() {
        return Optional.ofNullable(this.entryPoint);
    }

    /**
     * Sets the program's entry point. Throws if the entry point has previously been configured.
     */
    void setEntryPoint(MainMethodDeclaration declaration) {

        // Check whether a non-static method with the same name already exists in the current class
        assert !this.currentClassDeclarations.empty();

        MethodDeclaration previousDeclaration
            = this.methodDeclarations.get(this.currentClassDeclarations.peek()).get(declaration.getName());

        if (previousDeclaration != null) {
            fail(new RedeclarationException(declaration.getName(), declaration.getLocation(), previousDeclaration));
        }

        if (this.entryPoint != null) {
            fail(new RedeclarationException(declaration.getName(), declaration.getLocation(), this.entryPoint));
        }

        this.entryPoint = declaration;
    }


    // MARK: - Variable Reference Resolution

    private final SymbolTable symbolTable = new SymbolTable();

    void enterNewVariableDeclarationScope() {
        this.symbolTable.enterNewScope();
    }

    /**
     * Adds a new variable declaration to the current scope. Throws if a variable with the same name is already declared
     * and its declaration can't be shadowed.
     */
    void addVariableDeclarationToCurrentScope(VariableDeclaration declaration) {
        this.symbolTable.getVisibleDeclarationForName(declaration.getName()).ifPresent(previousDeclaration -> {

            if (this.symbolTable.isDeclarationInCurrentScope(declaration)) {
                throw fail(new RedeclarationException(declaration.getName(),
                    declaration.getLocation(), previousDeclaration));
            }

            if (!previousDeclaration.canBeShadowedByVariableDeclarationInNestedScope()) {
                throw fail(new RedeclarationException(declaration.getName(),
                    declaration.getLocation(), previousDeclaration,
                    "cannot shadow previous declaration"));
            }
        });

        this.symbolTable.enterDeclaration(declaration);
    }

    Optional<VariableDeclaration> getVariableDeclarationForName(String name) {
        return this.symbolTable.getVisibleDeclarationForName(name);
    }

    void leaveCurrentVariableDeclarationScope() {
        this.symbolTable.leaveCurrentScope();
    }

    void addGlobalVariableDeclaration(VariableDeclaration declaration) {
        VariableDeclaration globalDeclaration = this.globalVariableDeclarations.get(declaration.getName());

        if (globalDeclaration != null) {
            throw fail(new RedeclarationException(declaration.getName(), declaration.getLocation(), globalDeclaration,
                "redeclaration of global"));
        }

        this.globalVariableDeclarations.put(declaration.getName(), declaration);
    }

    Optional<VariableDeclaration> getGlobalVariableDeclarationForName(String name) {
        return Optional.ofNullable(this.globalVariableDeclarations.get(name));
    }

    // MARK: - Method and Field Reference Resolution

    private final Map<String, ClassDeclaration> classDeclarations = new HashMap<>();
    private final Map<ClassDeclaration, Map<String, MethodDeclaration>> methodDeclarations = new HashMap<>();
    private final Map<ClassDeclaration, Map<String, FieldDeclaration>> fieldDeclarations = new HashMap<>();

    void registerClassDeclaration(ClassDeclaration classDeclaration) {
        ClassDeclaration previousDeclaration = this.classDeclarations.get(classDeclaration.getName());
        if (previousDeclaration != null) {
            throw fail(new RedeclarationException(classDeclaration.getName(),
                classDeclaration.getLocation(), previousDeclaration));
        }

        this.classDeclarations.put(classDeclaration.getName(), classDeclaration);
        this.methodDeclarations.put(classDeclaration, new HashMap<>());
        this.fieldDeclarations.put(classDeclaration, new HashMap<>());
    }

    void registerMethodDeclaration(MethodDeclaration methodDeclaration, ClassDeclaration classDeclaration) {
        MethodDeclaration previousDeclaration
            = this.methodDeclarations.get(classDeclaration).get(methodDeclaration.getName());
        if (previousDeclaration != null) {
            throw fail(new RedeclarationException(methodDeclaration.getName(),
                                            methodDeclaration.getLocation(),
                                            previousDeclaration));
        }

        // Check that no entry point with the same name already exists
        if (this.entryPoint != null && methodDeclaration.getName().equals(this.entryPoint.getName())) {
            throw fail(new RedeclarationException(methodDeclaration.getName(),
                                            methodDeclaration.getLocation(),
                                            this.entryPoint));
        }

        this.methodDeclarations.get(classDeclaration).put(methodDeclaration.getName(), methodDeclaration);
    }

    void registerFieldDeclaration(FieldDeclaration fieldDeclaration, ClassDeclaration classDeclaration) {
        FieldDeclaration previousDeclaration
            = this.fieldDeclarations.get(classDeclaration).get(fieldDeclaration.getName());
        if (previousDeclaration != null) {
            throw fail(new RedeclarationException(fieldDeclaration.getName(),
                                            fieldDeclaration.getLocation(),
                                            previousDeclaration));
        }

        this.fieldDeclarations.get(classDeclaration).put(fieldDeclaration.getName(), fieldDeclaration);
    }

    Optional<BasicTypeDeclaration> getBasicTypeDeclarationForName(String name) {
        for (PrimitiveTypeDeclaration type : PrimitiveTypeDeclaration.values()) {
            if (name.equals(type.getName()) && type.canBeReferencedByUser()) {
                return Optional.of(type);
            }
        }

        return Optional.ofNullable(this.getClassDeclarationForName(name).orElse(null));
    }

    Optional<ClassDeclaration> getClassDeclarationForName(String name) {
        return Optional.ofNullable(this.classDeclarations.get(name));
    }

    Optional<MethodDeclaration> getMethodDeclarationForName(String name, ClassDeclaration container) {
        return Optional.ofNullable(this.methodDeclarations.get(container).get(name));
    }

    Optional<FieldDeclaration> getFieldDeclarationForName(String name, ClassDeclaration container) {
        return Optional.ofNullable(this.fieldDeclarations.get(container).get(name));
    }


    // MARK: - Compatibility

    // TODO: we should have unit tests for those

    static boolean canAssignTypeOfExpressionToTypeReference(TypeOfExpression type, TypeReference reference) {

        // type is not null
        if (type.getDeclaration().isPresent()) {

            // must be same basic type and number of dimensions
            if (type.getDeclaration().get() != reference.getBasicTypeReference().getDeclaration()) return false;
            if (type.getNumberOfDimensions() != reference.getNumberOfDimensions()) return false;

            return true;
        }

        // type is null
        else {
            if (reference.getNumberOfDimensions() >= 1) {
                return true;
            }
            else {
                return reference.getBasicTypeReference().getDeclaration() instanceof ClassDeclaration;
            }
        }
    }

    /** Generally not commutative. */
    static boolean canAssignTypeOfExpressionToTypeOfExpression(TypeOfExpression type, TypeOfExpression other) {

        // null type is not assignable
        if (!other.getDeclaration().isPresent()) {
            return false;
        }

        // type is not null
        if (type.getDeclaration().isPresent()) {

            // must be same basic type and number of dimensions
            if (type.getDeclaration().get() != other.getDeclaration().get()) return false;
            if (type.getNumberOfDimensions() != other.getNumberOfDimensions()) return false;

            return true;
        }

        // type is null
        else {
            if (other.getNumberOfDimensions() >= 1) {
                return true;
            }
            else {
                return other.getDeclaration().get() instanceof ClassDeclaration;
            }
        }
    }

    /** Should be commutative. */
    static boolean canCheckForEqualityWithTypesOfExpressions(TypeOfExpression left, TypeOfExpression right) {

        // left is not null
        if (left.getDeclaration().isPresent()) {

            // left is array or instance of some class
            if (left.getNumberOfDimensions() >= 1 || left.getDeclaration().get() instanceof ClassDeclaration) {

                // right is not null. must be (array of) same basic type and dimension.
                if (right.getDeclaration().isPresent()) {
                    if (right.getDeclaration().get() != left.getDeclaration().get()) return false;
                    if (right.getNumberOfDimensions() != left.getNumberOfDimensions()) return false;

                    return true;
                }

                // right is null. valid.
                else {
                    return true;
                }
            }

            // left is primitive type
            else {

                // right is not null. must be same basic type, but not void.
                if (right.getDeclaration().isPresent()) {
                    if (left.getDeclaration().get() == PrimitiveTypeDeclaration.VOID) return false;
                    if (right.getDeclaration().get() != left.getDeclaration().get()) return false;
                    if (right.getNumberOfDimensions() != 0) return false;

                    return true;
                }

                // right is null. invalid.
                else {
                    return false;
                }
            }
        }

        // left is null
        else {

            // right is not null. must be array or instance of some class.
            if (right.getDeclaration().isPresent()) {
                if (right.getNumberOfDimensions() >= 1) {
                    return true;
                }
                else {
                    return right.getDeclaration().get() instanceof ClassDeclaration;
                }
            }

            // right is null. valid.
            else {
                return true;
            }
        }
    }

    static WrappedSemanticException fail(SemanticException exception) {
        throw new WrappedSemanticException(exception);
    }
}
