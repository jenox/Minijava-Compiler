package edu.kit.minijava.backend.instructions;

import edu.kit.minijava.backend.*;

public class ConditionalJump extends Jump {
    private String opcodeMnemonic;

    public ConditionalJump(String opcodeMnemonic, BasicBlock targetBlock) {
        super(targetBlock);
        this.opcodeMnemonic = opcodeMnemonic;
    }

    public String getOpcodeMnemonic() {
        return this.opcodeMnemonic;
    }

    @Override
    public String emitIntermediateInstruction() {
        return INDENT + opcodeMnemonic + " " + this.getTargetBlock().formatBlockLabel();
    }
}
