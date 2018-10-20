package edu.kit.minijava.parser;

import java.util.*;

public final class MethodInvocation extends PostfixOperation {
    public MethodInvocation(String methodName, List<Expression> arguments) {
        this.methodName = methodName;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public final String methodName;
    public final List<Expression> arguments;
}
