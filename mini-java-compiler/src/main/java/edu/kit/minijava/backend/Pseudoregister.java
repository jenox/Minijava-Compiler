package edu.kit.minijava.backend;

public class Pseudoregister {

    private static int regularPseudoRegisterCount = 0;
    private static int phiPseudoRegisterCount = -1;

    public static int getPseudoRegisterNumber() {
        return regularPseudoRegisterCount++;
    }

    public static int getPhiRegisterNumber() {
        return phiPseudoRegisterCount--;
    }

    public static void setNumberOfPseudoRegisters(int number) {
        regularPseudoRegisterCount = number;
    }

    public static void setPhiRegisterNumber(int number) {
        phiPseudoRegisterCount = number;
    }

}
