package edu.kit.minijava.parser;

public class PropagatedException extends ParserException {
    PropagatedException(Throwable cause) {
        this.cause = cause;
    }

    final Throwable cause;
}
