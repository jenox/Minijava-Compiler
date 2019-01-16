package edu.kit.minijava.backend;

import firm.Mode;

public class Util {
    /*
     * UTILITY FUNCTIONS
     */

    public static final String INDENT = "    "; // 4 spaces

    public static String mode2RegSuffix(Mode mode) {
        if (mode.equals(Mode.getIs())) {
            return "d";
        }
        else if (mode.equals(Mode.getBs())) {
            return "l";
        }

        return "";
    }

    public static String mode2MovSuffix(Mode mode) {
        if (mode.equals(Mode.getIs())) {
            return "l";
        }
        else if (mode.equals(Mode.getBs())) {
            return "b";
        }
        else if (mode.equals(Mode.getP())
                        || mode.equals(Mode.getLs())) {
            return "q"; //TODO: is q correct choice for mode Ls?
        }

        return "";
    }
}
