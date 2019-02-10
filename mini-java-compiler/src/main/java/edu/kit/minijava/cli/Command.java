package edu.kit.minijava.cli;

public abstract class Command {

    private CompilerFlags flags;

    Command(CompilerFlags flags) {
        if (flags != null) {
            this.flags = flags;
        }
        else {
            this.flags = CompilerFlags.getStandardFlags();
        }
    }

    protected CompilerFlags getFlags() {
        return this.flags;
    }

    public abstract int execute(String path);
}
