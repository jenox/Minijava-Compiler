package edu.kit.minijava.cli;

public class CompilerFlags {

    private boolean dumpIntermediates;
    private boolean optimize;
    private boolean verbose;

    public CompilerFlags(boolean dumpIntermediates,
                         boolean optimize,
                         boolean verbose) {
        this.dumpIntermediates = dumpIntermediates;
        this.optimize = optimize;
        this.verbose = verbose;
    }

    public boolean dumpIntermediates() {
        return this.dumpIntermediates;
    }

    public boolean optimize() {
        return this.optimize;
    }

    public boolean beVerbose() {
        return this.verbose;
    }

    public static CompilerFlags getStandardFlags() {
        return new CompilerFlags(false, true, false);
    }
}
