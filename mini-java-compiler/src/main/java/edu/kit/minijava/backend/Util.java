package edu.kit.minijava.backend;

import firm.BackEdges;
import firm.Mode;
import firm.Relation;
import firm.nodes.Phi;

public class Util {
    /*
     * UTILITY FUNCTIONS
     */

    public static final String INDENT = "    "; // 4 spaces

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

    public static boolean containsJmp(String str) {
        return str.contains(Util.INDENT + "jmp ")
                        || str.contains(Util.INDENT + "jle ")
                        || str.contains(Util.INDENT + "jl ")
                        || str.contains(Util.INDENT + "jge ")
                        || str.contains(Util.INDENT + "jg ")
                        || str.contains(Util.INDENT + "jne ")
                        || str.contains(Util.INDENT + "je ");
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

    public static boolean isLoopingPhi(Phi phi) {
        for (BackEdges.Edge edge : BackEdges.getOuts(phi)) {
            if (edge.node instanceof Phi && edge.node.getNr() < phi.getNr()) {
                return true;
            }
        }

        return false;
    }
}
