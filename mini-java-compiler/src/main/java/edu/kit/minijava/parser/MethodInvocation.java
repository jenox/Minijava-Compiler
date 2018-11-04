package edu.kit.minijava.parser;

import java.util.Collections;
import java.util.List;

public final class MethodInvocation extends PostfixOperation {
    public MethodInvocation(String methodName, List<Expression> arguments) {
        if (methodName == null) throw new IllegalArgumentException();
        if (arguments == null) throw new IllegalArgumentException();

        this.methodName = methodName;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public final String methodName;
    public final List<Expression> arguments;

    @Override
    public String toString() {
        return "MethodInvocation(" + this.methodName + ", " + this.arguments + ")";
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
