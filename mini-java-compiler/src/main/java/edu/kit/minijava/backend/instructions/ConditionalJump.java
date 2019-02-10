package edu.kit.minijava.backend.instructions;

import edu.kit.minijava.backend.*;

/**
 * A jump instruction that jumps to another basic block and is predicated on a condition.
 */
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
