package edu.kit.minijava.semantic;

import edu.kit.minijava.lexer.TokenLocation;

public class TypeMismatchException extends SemanticException {

    public TypeMismatchException(String actualTypeName, TokenLocation location, String purpose, String context,
                                 String... expectedTypeNames) {

        this.actualTypeName = actualTypeName;
        this.location = location;
        this.purpose = purpose;
        this.context = context;
        this.expectedTypeNames = expectedTypeNames;
    }

    private String actualTypeName; // nullable
    private TokenLocation location; // nullable
    private String purpose;
    private String context; // nullable
    private String[] expectedTypeNames;

    public String getActualTypeName() {
        return this.actualTypeName;
    }

    public TokenLocation getLocation() {
        return this.location;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public String getContext() {
        return this.context;
    }

    public String[] getExpectedTypeNames() {
        return this.expectedTypeNames;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();

        builder.append("Mismatching types");

        if (this.context != null) {
            builder.append(" for ");
            builder.append(this.context);
        }

        if (this.location != null) {
            builder.append(" at ");
            builder.append(this.location);
        }
        builder.append(": ");

        if (this.actualTypeName != null) {
            builder.append("encountered ");
            builder.append(this.actualTypeName);
        }

        if (this.expectedTypeNames != null && this.expectedTypeNames.length > 0) {
            builder.append(", but");
            builder.append("expected");
            builder.append(String.join(", ", this.expectedTypeNames));
        }

        if (this.context != null) {
            builder.append(" (in ");
            builder.append(this.context);
            builder.append(")");
        }
        builder.append(".");

        return builder.toString();
    }
}
