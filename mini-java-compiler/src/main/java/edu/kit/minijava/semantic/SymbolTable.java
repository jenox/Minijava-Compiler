package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class SymbolTable {
    SymbolTable() {
    }

    private final Stack<Map<String, VariableDeclaration>> scopes = new Stack<>();

    public int getNumberOfScopes() {
        return this.scopes.size();
    }

    public void enterNewScope() {
        this.scopes.push(new HashMap<>());
    }

    public void leaveCurrentScope() {
        assert !this.scopes.isEmpty();

        this.scopes.pop();
    }

    public void enterDeclaration(VariableDeclaration declaration) {
        assert !this.scopes.isEmpty();

        // TODO: make this a checked exception (redefinition in same scope)
        assert !this.scopes.peek().containsKey(declaration.getName());

        // TODO: where to we catch other invalid redeclarations?

        this.scopes.peek().put(declaration.getName(), declaration);
    }

    public Optional<VariableDeclaration> getVisibleDeclarationForName(String name) {
        for (int i = this.scopes.size() - 1; i >= 0; i--) {
            VariableDeclaration declaration = this.scopes.get(i).get(name);

            if (declaration != null) {
                return Optional.of(declaration);
            }
        }

        return Optional.empty();
    }

    public boolean hasVariableDeclarationInCurrentScopeWithName(String name) {
        return this.scopes.peek().containsKey(name);
    }
}
