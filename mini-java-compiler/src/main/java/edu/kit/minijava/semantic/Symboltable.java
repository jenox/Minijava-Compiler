package edu.kit.minijava.semantic;

import edu.kit.minijava.ast.nodes.Declaration;

import java.util.HashMap;
import java.util.Optional;
import java.util.Stack;

public class Symboltable {
    private Stack<HashMap<String, Declaration>> scopes = new Stack<>();

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void leaveScope() {
        scopes.pop();
    }

    public void enterDeclaration(String ident, Declaration decl) {
        scopes.peek().put(ident, decl);
        System.out.println(scopes);
    }

    public Optional<Declaration> getCurrentDeclaration(String ident) {
        for (HashMap<String, Declaration> iterScope : scopes) {
            if (iterScope.get(ident) != null)
                return Optional.of(iterScope.get(ident));
        }

        return Optional.empty();
    }

    public boolean isDeclarationInCurrentScope(String ident) {
        return getCurrentDeclaration(ident).isPresent();
    }

}
