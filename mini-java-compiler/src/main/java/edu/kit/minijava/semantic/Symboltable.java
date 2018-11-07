package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.*;

import java.util.*;

public class Symboltable {
    Symboltable() {
    }

    private final Stack<Map<String, VariableDeclaration>> scopes = new Stack<>();

    public void enterScope() {
        this.scopes.push(new HashMap<>());
    }

    public void leaveScope() {
        this.scopes.pop();
    }

    public void enterDeclaration(String ident, VariableDeclaration decl) {
        this.scopes.peek().put(ident, decl);
    }

    public Optional<VariableDeclaration> getCurrentDeclaration(String ident) {
        for (int i = this.scopes.size() - 1; i >= 0 ; i--) {
            VariableDeclaration temp = this.scopes.get(i).get(ident);
            if (temp != null)
                return Optional.of(temp);
        }

        return Optional.empty();
    }

    public boolean isDeclarationInCurrentScope(String ident) {
        return this.getCurrentDeclaration(ident).isPresent();
    }

}
