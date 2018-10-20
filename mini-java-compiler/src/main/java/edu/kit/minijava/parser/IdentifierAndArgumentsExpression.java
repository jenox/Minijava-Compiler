package edu.kit.minijava.parser;

import java.util.*;

public final class IdentifierAndArgumentsExpression extends Expression {
    public IdentifierAndArgumentsExpression(String identifier, List<Expression> arguments) {
        this.identifier = identifier;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public final String identifier;
    public final List<Expression> arguments;
}
