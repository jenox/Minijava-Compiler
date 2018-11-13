package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

abstract class SemanticAnalysisVisitorBase extends ASTVisitor<Void> {
    SemanticAnalysisVisitorBase() {
    }


    // MARK: - General State

    private boolean isCollectingDeclarationsForUseBeforeDeclare = true;
    private final Stack<ClassDeclaration> currentClassDeclarations = new Stack<>();
    private final Stack<SubroutineDeclaration> currentMethodDeclarations = new Stack<>();

    boolean isCollectingDeclarationsForUseBeforeDeclare() {
        return this.isCollectingDeclarationsForUseBeforeDeclare;
    }

    void finishCollectingDeclarationsForUseBeforeDeclare() {
        this.isCollectingDeclarationsForUseBeforeDeclare = false;
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
        assert this.entryPoint == null : "invalid redeclaration of entry point";

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
            assert !this.symbolTable.isDeclarationInCurrentScope(declaration) : "invalid redeclaration";
            assert previousDeclaration.canBeShadowedByVariableDeclarationInNestedScope() : "cannot shadow prev decl";
        });

        this.symbolTable.enterDeclaration(declaration);
    }

    Optional<VariableDeclaration> getVariableDeclarationForName(String name) {
        return this.symbolTable.getVisibleDeclarationForName(name);
    }

    void leaveCurrentVariableDeclarationScope() {
        this.symbolTable.leaveCurrentScope();
    }


    // MARK: - Method and Field Reference Resolution

    private final Map<String, ClassDeclaration> classDeclarations = new HashMap<>();
    private final Map<ClassDeclaration, Map<String, MethodDeclaration>> methodDeclarations = new HashMap<>();
    private final Map<ClassDeclaration, Map<String, FieldDeclaration>> fieldDeclarations = new HashMap<>();

    void registerClassDeclaration(ClassDeclaration classDeclaration) {
        assert !this.classDeclarations.containsKey(classDeclaration.getName()) : "invalid redeclaration of class";

        this.classDeclarations.put(classDeclaration.getName(), classDeclaration);
        this.methodDeclarations.put(classDeclaration, new HashMap<>());
        this.fieldDeclarations.put(classDeclaration, new HashMap<>());
    }

    void registerMethodDeclaration(MethodDeclaration methodDeclaration, ClassDeclaration classDeclaration) {
        assert !this.methodDeclarations.get(classDeclaration).containsKey(methodDeclaration.getName()) :
                "invalid redeclaration of method";

        this.methodDeclarations.get(classDeclaration).put(methodDeclaration.getName(), methodDeclaration);
    }

    void registerFieldDeclaration(FieldDeclaration fieldDeclaration, ClassDeclaration classDeclaration) {
        assert !this.fieldDeclarations.get(classDeclaration).containsKey(fieldDeclaration.getName()) :
                "invalid redeclaration of field";

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
}
