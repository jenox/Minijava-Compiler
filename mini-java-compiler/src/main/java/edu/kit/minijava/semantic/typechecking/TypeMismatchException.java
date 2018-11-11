package edu.kit.minijava.semantic.typechecking;

import edu.kit.minijava.semantic.SemanticAnalysisException;

public class TypeMismatchException extends SemanticAnalysisException {

    public TypeMismatchException(String message) {
        super(message);
    }

}
