package edu.kit.minijava.backend;

import edu.kit.minijava.backend.instructions.*;

import java.util.*;

public class CriticalEdgeRemover {

    public CriticalEdgeRemover() {}

    public static List<BasicBlock> removeCriticalEdges(List<BasicBlock> basicBlocks) {
        List<BasicBlock> result = new ArrayList<>();

        // Iterate through all blocks and remove critical edges for each of them

        for (BasicBlock currentBlock : basicBlocks) {

            result.add(currentBlock);

            List<PhiNode> phiNodeList = currentBlock.getPhiNodes();

            if (phiNodeList.size() > 0) {

                // Iterate through all predecessor of the phis.
                // This should be the same number for all Phi nodes in a sane graph.

                for (int i = 0; i < phiNodeList.get(0).getMappings().size(); i++) {
                    PhiNode.Mapping mapping = phiNodeList.get(0).getMappings().get(i);
                    BasicBlock pred = mapping.getBlock();

                    // The edge can only be critical if its starting point has multiple edges that start there.
                    // (In fact, it is critical exactly then, as otherwise, there would not be any Phi node here.)

                    if (!pred.hasBranchingControlFlow()) {
                        continue;
                    }

                    BasicBlock connectingBlock = createNewConnectionBlock(pred, currentBlock);
                    result.add(connectingBlock);

                    for (PhiNode phiNode : phiNodeList) {
                        phiNode.modifySourceBlock(i, connectingBlock);
                    }
                }
            }
        }

        return result;
    }

    public static BasicBlock createNewConnectionBlock(BasicBlock pred, BasicBlock target) {

        BasicBlock connectingBlock = new BasicBlock();

        // First, handle the conditional jump, if there is one
        if (pred.getConditionalJump().isPresent()) {

            BasicBlock targetBlock = pred.getConditionalJump().get().getTargetBlock();
            int targetBlockNumber = targetBlock.getBlockLabel();

            if (targetBlockNumber == target.getBlockLabel()) {

                // In this case we can replace the conditional jump with a conditional jump to the new block
                // and connect the connecting block with the target block

                ConditionalJump newConditionalJump
                    = new ConditionalJump(pred.getConditionalJump().get().getOpcodeMnemonic(), connectingBlock);
                pred.setConditionalJump(newConditionalJump);

                // Connect the new block to the original target of the jump
                Jump connectingJump = new Jump(target);
                connectingBlock.setEndJump(connectingJump);

                return connectingBlock;
            }

            // Simply retain all other jumps
        }

        // Now handle the unconditional jump which should always be present

        if (pred.getEndJump().isPresent()) {

            BasicBlock targetBlock = pred.getEndJump().get().getTargetBlock();
            int targetBlockNumber = targetBlock.getBlockLabel();

            if (targetBlockNumber == target.getBlockLabel()) {

                Jump firstConnectingJump = new Jump(connectingBlock);
                pred.setEndJump(firstConnectingJump);

                Jump secondConnectingJump = new Jump(target);
                connectingBlock.setEndJump(secondConnectingJump);

                return connectingBlock;
            }
        }

        return connectingBlock;
    }
}
