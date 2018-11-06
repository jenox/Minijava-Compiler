package edu.kit.minijava.parser;

public class PropagatedException extends ParserException {
    PropagatedException(Throwable cause) {
        this.cause = cause;
    }

    private final Throwable cause;

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
