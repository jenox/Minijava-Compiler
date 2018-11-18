package edu.kit.minijava.semantic;

/**
 * An exception that describes a semantic error that is wrapped in an unchecked exception
 * to allow it to be non-intrusively used in code that uses the visitor pattern.
 */
public class WrappedSemanticException extends RuntimeException {
    WrappedSemanticException(SemanticException exception) {
        this.exception = exception;
        if (exception == null) throw new IllegalArgumentException();
    }

    private SemanticException exception;

    public SemanticException getException() {
        return this.exception;
    }
}
