package edu.kit.minijava.parser;

import java.util.Collections;
import java.util.List;

public final class IdentifierAndArgumentsExpression extends Expression {
    public IdentifierAndArgumentsExpression(String identifier, List<Expression> arguments) {
        if (identifier == null) throw new IllegalArgumentException();
        if (arguments == null) throw new IllegalArgumentException();

        this.identifier = identifier;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public final String identifier;
    public final List<Expression> arguments;

    @Override
    public String toString() {
        return "IdentifierAndArgumentsExpression(" + this.identifier + ", " + this.arguments + ")";
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
