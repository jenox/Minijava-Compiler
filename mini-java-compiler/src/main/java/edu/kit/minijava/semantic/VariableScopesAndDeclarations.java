package edu.kit.minijava.semantic;

import java.util.*;

import edu.kit.minijava.ast.nodes.VariableDeclaration;

/**
 * Data structure to maintain declarations and their scopes of a variable.
 *
 */
public class VariableScopesAndDeclarations {

    private List<VariableDeclaration> variableDeclaration;
    private List<Integer> scopes;

    /**
     * Constuctor should be called only the first time variable is seen. For following declarations
     * use {@link #addVariableDeclaration(VariableDeclaration, int)} of already created instance.
     * @param initialVarDecl initial variable declaration
     * @param initialScope number of scope declaration was found in.
     */
    public VariableScopesAndDeclarations(VariableDeclaration initialVarDecl, int initialScope) {
        this.variableDeclaration = new ArrayList<>();
        this.variableDeclaration.add(initialVarDecl);
        this.scopes = new ArrayList<>();
        this.scopes.add(initialScope);
    }

    /**
     * Add a new variable declaration into a numbered scope
     * @param varDecl the variable declaration to enter into the scope provided as parameter
     * @param scope the scope number to enter the declaration into
     */
    public void addVariableDeclaration(VariableDeclaration varDecl, int scope) {
        int maxScope = this.scopes.get(this.scopes.size() - 1);
        assert scope != maxScope : "Variable already declared in this scope";
        assert scope > maxScope : "scopes should be sorted";

        this.variableDeclaration.add(varDecl);
        this.scopes.add(scope);
    }

    public void removeScope(int scope) {
        assert this.scopes.size() > 0;

        int last = this.scopes.size() - 1;

        assert this.scopes.get(last) == scope : "not current scope";
        this.variableDeclaration.remove(last);
        this.scopes.remove(last);
    }

    public VariableDeclaration getLastDeclaration() {
        assert this.scopes.size() > 0;

        int last = this.scopes.size() - 1;
        return this.variableDeclaration.get(last);
    }

    public boolean isEmpty() {
        return this.scopes.isEmpty();
    }



}
