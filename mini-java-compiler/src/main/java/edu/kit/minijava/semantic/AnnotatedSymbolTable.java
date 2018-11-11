package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class AnnotatedSymbolTable<T extends Declaration> {

    // TODO Maybe use this instead of separate Maps for global and local declarations?
//    public static enum ScopeType {
//        GLOBAL,
//        MEMBER,
//        LOCAL;
//    }

    private final Stack<Map<String, T>> globalScopes = new Stack<>();
    private final Stack<Map<String, T>> localScopes = new Stack<>();

    public void enterNewLocalScope() {
        this.localScopes.push(new HashMap<>());
    }

    public void enterNewGlobalScope() {
        this.globalScopes.push(new HashMap<>());
    }

    public void leaveCurrentScope() {
        assert !this.localScopes.isEmpty() || !this.globalScopes.isEmpty();

        if (!this.localScopes.isEmpty()) {
            this.localScopes.pop();
        }
        else {
            this.globalScopes.pop();
        }
    }

    public void enterDeclaration(String identifier, T declaration) throws RedeclarationException {
        assert !this.globalScopes.isEmpty() || !this.localScopes.isEmpty();

        Map<String, T> upperDeclarations = !this.localScopes.isEmpty() ? localScopes.peek() : globalScopes.peek();

        // Ensure that at least one scope is on either of the stacks
        assert declaration != null;

        // Catch case that local declaration is already defined
        if (getVisibleDeclarationForIdentifer(identifier).isPresent()) {
            // Found local declaration
            // TODO Add location of the redeclaration
            throw new RedeclarationException(declaration, null);
        }

        // TODO Rework this to be more general
        if (!this.localScopes.isEmpty()) {
            this.localScopes.peek().put(identifier, declaration);
        }
        else {
            this.globalScopes.peek().put(identifier, declaration);
        }
    }

    public void addAllDeclarations(Map<String, ? extends T> declarations) throws RedeclarationException {

        // Add all new declarations to current scope
        Map<String, T> currentScope = this.localScopes.isEmpty() ? this.globalScopes.peek() : this.localScopes.peek();

        for (Map.Entry<String, ? extends T> entry : declarations.entrySet()) {
            currentScope.put(entry.getKey(), entry.getValue());
        }
    }

    public Optional<T> getVisibleLocalDeclarationForIdentifier(String identifier) {
        for (int i = this.localScopes.size() - 1; i >= 0; i--) {
            T declaration = this.localScopes.get(i).get(identifier);

            if (declaration != null) {
                return Optional.of(declaration);
            }
        }

        return Optional.empty();
    }

    public Optional<T> getVisibleDeclarationForIdentifer(String identifier) {

        Optional<T> optionalLocalDeclaration = this.getVisibleLocalDeclarationForIdentifier(identifier);

        if (optionalLocalDeclaration.isPresent()) {
            return optionalLocalDeclaration;
        }
        else {
            // Search in global scope
            for (int i = this.globalScopes.size() - 1; i >= 0; i--) {
                T declaration = this.globalScopes.get(i).get(identifier);

                if (declaration != null) {
                    return Optional.of(declaration);
                }
            }

            return Optional.empty();
        }
    }
}
