package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public final class ClassAndMemberNameConflictChecker {
    public ClassAndMemberNameConflictChecker(Program program) {
        this.classDeclarations = new HashMap<>();
        this.instanceMethodDeclarations = new HashMap<>();
        this.fieldDeclarations = new HashMap<>();
        this.entryPointDeclaration = null;

        for (ClassDeclaration declaration : program.getClassDeclarations()) {
            this.add(declaration);
        }

        assert this.entryPointDeclaration != null : "missing entry point declaration";
    }

    private final Map<String, ClassDeclaration> classDeclarations;
    private final Map<ClassDeclaration, Map<String, MethodDeclaration>> instanceMethodDeclarations;
    private final Map<ClassDeclaration, Map<String, FieldDeclaration>> fieldDeclarations;
    private MainMethodDeclaration entryPointDeclaration;

    public ClassDeclaration getClassDeclaration(String name) {
        return this.classDeclarations.get(name);
    }

    public MethodDeclaration getInstanceMethodDeclaration(String name, ClassDeclaration container) {
        return this.instanceMethodDeclarations.get(container).get(name);
    }

    public FieldDeclaration getFieldDeclaration(String name, ClassDeclaration container) {
        return this.fieldDeclarations.get(container).get(name);
    }

    private void add(ClassDeclaration classDeclaration) {
        if (this.classDeclarations.containsKey(classDeclaration.getName())) {
            assert false : "invalid redeclaration of class " + classDeclaration.getName();
        }

        Map<String, MethodDeclaration> instanceMethodDeclarations = new HashMap<>();
        Map<String, FieldDeclaration> fieldDeclarations = new HashMap<>();

        for (MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) {
            if (instanceMethodDeclarations.containsKey(methodDeclaration.getName())) {
                assert false : "invalid redeclaration of instance method " + methodDeclaration.getName();
            }

            instanceMethodDeclarations.put(methodDeclaration.getName(), methodDeclaration);
        }

        for (FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) {
            if (fieldDeclarations.containsKey(fieldDeclaration.getName())) {
                assert false : "invalid redeclaration of field " + fieldDeclaration.getName();
            }

            fieldDeclarations.put(fieldDeclaration.getName(), fieldDeclaration);
        }

        for (MainMethodDeclaration methodDeclaration : classDeclaration.getMainMethodDeclarations()) {
            assert methodDeclaration.getName().equals("main") : "entry point must be named main";

            if (this.entryPointDeclaration != null) {
                assert false : "invalid redeclaration of entry point";
            }
            else {
                this.entryPointDeclaration = methodDeclaration;
            }
        }

        this.classDeclarations.put(classDeclaration.getName(), classDeclaration);
        this.instanceMethodDeclarations.put(classDeclaration, instanceMethodDeclarations);
        this.fieldDeclarations.put(classDeclaration, fieldDeclarations);
    }
}
