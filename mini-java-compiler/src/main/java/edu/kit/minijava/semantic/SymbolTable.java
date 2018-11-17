package edu.kit.minijava.semantic;

import java.util.*;

import edu.kit.minijava.ast.nodes.VariableDeclaration;

// TODO: Put this logic straight into SemanticAnalysisBase?
class SymbolTable {

    private final HashMap<String, VariableScopesAndDeclarations> nameToDeclarations = new HashMap<>();
    private final HashMap<Integer, Set<String>> scopeToNames = new HashMap<>();

    public SymbolTable() {
        this.currentScope = 0;
    }

    private int currentScope;

    void enterNewScope() {
        this.currentScope++;
    }

    void leaveCurrentScope() {
        assert this.currentScope > 0 : "left more scopes than entered";

        Set<String> definedVars = this.scopeToNames.get(this.currentScope);

        if (definedVars != null) {

            for (String var : definedVars) {
                VariableScopesAndDeclarations scope = this.nameToDeclarations.get(var);
                scope.removeLastScope();

                if (scope.isEmpty()) {
                    this.nameToDeclarations.remove(var);
                }
            }

            // remove entry from map
            this.scopeToNames.remove(this.currentScope);

        }

        this.currentScope--;

    }

    void enterDeclaration(VariableDeclaration declaration) {
        String name = declaration.getName();
        if (this.nameToDeclarations.containsKey(name)) {
            VariableScopesAndDeclarations scope = this.nameToDeclarations.get(name);
            scope.addVariableDeclaration(declaration, this.currentScope);
        }

        // name is not in map
        else {
            VariableScopesAndDeclarations scope = new VariableScopesAndDeclarations(declaration, this.currentScope);
            this.nameToDeclarations.put(name, scope);

            // add name to scopeToNames
            if (this.scopeToNames.containsKey(scope)) {
                this.scopeToNames.get(scope).add(name);
            }
            else {
                HashSet<String> set = new HashSet<>();
                set.add(name);
                this.scopeToNames.put(this.currentScope, set);
            }
        }
    }

    Optional<VariableDeclaration> getVisibleDeclarationForName(String name) {

        if (this.nameToDeclarations.containsKey(name)) {
            VariableDeclaration declaration = this.nameToDeclarations.get(name).getLastDeclaration();
            return Optional.of(declaration);
        }
        else {
            return Optional.empty();
        }

    }

    boolean isDeclarationInCurrentScope(VariableDeclaration declaration) {
        String name = declaration.getName();
        Set<String> definedVars = this.scopeToNames.get(this.currentScope);

        if (definedVars == null) {
            return false;
        }
        else {
            return definedVars.contains(name);
        }
    }
}
