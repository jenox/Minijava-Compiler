package edu.kit.minijava.backend;

import firm.Mode;

public class Util {
    /*
     * UTILITY FUNCTIONS
     */


    public String mode2RegSuffix(Mode mode) {
        if (mode.equals(Mode.getIs())) {
            return "d";
        }
        else if (mode.equals(Mode.getBs())) {
            return "l";
        }

        return "";
    }

    public String mode2MovSuffix(Mode mode) {
        if (mode.equals(Mode.getIs())) {
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
}
