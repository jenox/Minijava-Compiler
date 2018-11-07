package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class Symboltable {
    Symboltable() {
    }

    private final Stack<Map<String, VariableDeclaration>> scopes = new Stack<>();

    public void enterNewScope() {
        this.scopes.push(new HashMap<>());
    }

    public void leaveCurrentScope() {
        assert !this.scopes.isEmpty();

        this.scopes.pop();
    }

    public void enterDeclaration(String identifier, VariableDeclaration declaration) {
        assert !this.scopes.isEmpty();

        // TODO: make this a checked exception (redefinition in same scope)
        assert !this.scopes.peek().containsKey(identifier);

        // TODO: where to we catch other invalid redeclarations?

        this.scopes.peek().put(identifier, declaration);
    }

    public Optional<VariableDeclaration> getVisibleDeclarationForIdentifer(String identifier) {
        for (int i = this.scopes.size() - 1; i >= 0; i--) {
            VariableDeclaration declaration = this.scopes.get(i).get(identifier);

            if (declaration != null) {
                return Optional.of(declaration);
            }
        }

        return Optional.empty();
    }
}
