package edu.kit.minijava.backend;

public class PseudoregisterCounter {

    private static int regularPseudoRegisterCount = 0;
    private static int phiPseudoRegisterCount = -1;

    /**
     * Prohibite construction of static utility class
     */
    private PseudoregisterCounter() {

    }

    public static int getPseudoregisterNumber() {
        return regularPseudoRegisterCount++;
    }

    public static int getPhiRegisterNumber() {
        return phiPseudoRegisterCount--;
    }

    public static void setNumberOfPseudoregisters(int number) {
        regularPseudoRegisterCount = number;
    }

    public static void setPhiRegisterNumber(int number) {
        phiPseudoRegisterCount = number;
    }

}
