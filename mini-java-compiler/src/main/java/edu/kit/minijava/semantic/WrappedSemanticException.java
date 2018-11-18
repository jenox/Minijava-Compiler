package edu.kit.minijava.semantic;

public class WrappedSemanticException extends RuntimeException {
    public WrappedSemanticException(SemanticException exception) {
        this.exception = exception;
        if (exception == null) throw new IllegalArgumentException();
    }

    private SemanticException exception;

    public SemanticException getException() {
        return this.exception;
    }
}