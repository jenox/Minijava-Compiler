package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

// TODO: Put this logic straight into SemanticAnalysisBase?
class SymbolTable {
    SymbolTable() {
    }

    private final Stack<Map<String, VariableDeclaration>> scopes = new Stack<>();

    void enterNewScope() {
        this.scopes.push(new HashMap<>());
    }

    void leaveCurrentScope() {
        assert !this.scopes.isEmpty();

        this.scopes.pop();
    }

    void enterDeclaration(VariableDeclaration declaration) {
        assert !this.scopes.isEmpty();

        this.scopes.peek().put(declaration.getName(), declaration);
    }

    Optional<VariableDeclaration> getVisibleDeclarationForName(String name) {
        for (int i = this.scopes.size() - 1; i >= 0; i--) {
            VariableDeclaration declaration = this.scopes.get(i).get(name);

            if (declaration != null) {
                return Optional.of(declaration);
            }
        }

        return Optional.empty();
    }

    boolean isDeclarationInCurrentScope(VariableDeclaration declaration) {
        return this.scopes.peek().containsValue(declaration);
    }
}
