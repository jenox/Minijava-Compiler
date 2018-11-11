package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class SymbolTable<T extends VariableDeclaration> {

    private final Stack<Map<String, T>> scopes = new Stack<>();

    public void enterNewScope() {
        this.scopes.push(new HashMap<>());
    }

    public void leaveCurrentScope() {
        if (this.scopes.isEmpty()) {
            throw new IllegalStateException("Cannot leave scope without any scope present.");
        }

        this.scopes.pop();
    }

    public void enterDeclaration(String identifier, T declaration) throws RedeclarationException {

        if (this.scopes.isEmpty()) {
            throw new IllegalStateException("Cannot enter declaration without any scope");
        }

        Optional<T> previousDeclaration = this.getVisibleDeclarationForIdentifier(identifier);

        if (previousDeclaration.isPresent() && !previousDeclaration.get().canDeclarationBeShadowed()) {
            // TODO Include position.
            throw new RedeclarationException(declaration, null);
        }

        this.scopes.peek().put(identifier, declaration);
    }

    public void enterAllDeclarations(Map<String, ? extends T> declarations) throws RedeclarationException {

        for (Map.Entry<String, ? extends T> entry : declarations.entrySet()) {
            this.enterDeclaration(entry.getKey(), entry.getValue());
        }
    }

    public Optional<T> getVisibleDeclarationForIdentifier(String identifier) {
        for (int i = this.scopes.size() - 1; i >= 0; i--) {
            T declaration = this.scopes.get(i).get(identifier);

            if (declaration != null) {
                return Optional.of(declaration);
            }
        }

        return Optional.empty();
    }
}
