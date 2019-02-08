package edu.kit.minijava.backend;

import firm.*;

/**
 * Utility functions for code generation.
 */
public class Util {

    /**
     * Static utility class should not be instantiated.
     */
    private Util() {
    }

    public static String mode2RegSuffix(Mode mode) {
        if (mode.equals(Mode.getIs()) || mode.equals(Mode.getLs())) {
            return "d";
        }
        else if (mode.equals(Mode.getBs())) {
            return "l";
        }

        return "";
    }

    public static String mode2MovSuffix(Mode mode) {
        if (mode.equals(Mode.getIs()) || mode.equals(Mode.getLs())) {
            return "l";
        }
        else if (mode.equals(Mode.getBs())) {
            return "b";
        }
        else if (mode.equals(Mode.getP())) {
            return "q";
        }

        return "";
    }

    public static String relation2Jmp(Relation relation) {
        switch (relation) {
            case Less:
                return "jl";
            case LessEqual:
                return "jle";
            case Greater:
                return "jg";
            case GreaterEqual:
                return "jge";
            case Equal:
                return "je";
            case LessGreater:
            case UnorderedLessGreater:
                return "jne";
            default:
                // This should not happen
                assert false : "Unknown relation in cond node code generation!";
                return "";
        }
    }
}
