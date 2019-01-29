package edu.kit.minijava.backend;

import edu.kit.minijava.backend.instructions.*;

import java.util.*;
import java.util.stream.Collectors;

public class PhiResolver {

    public static void resolvePhiNodes(BasicBlock block) {
        List<PhiNode> phiNodeList = block.getPhiNodes();

        if (phiNodeList.isEmpty()) {
            // If there are no Phi nodes, we are already done here
            return;
        }

        for (int i = 0; i < phiNodeList.get(0).getMappings().size(); i++) {
            // If we select one predecessor index, all predecessor blocks for the Phi nodes should be the same
            // TODO Maybe alidate this here as well

            PhiNode firstPhi = phiNodeList.get(0);

            BasicBlock predBlock = firstPhi.getMappings().get(i).getBlock();

            // Check for sanity: Removal of critical edges should already be completed at this point
            assert !predBlock.hasBranchingControlFlow()
                : "Critical edge found during resolving Phis, please remove first!";

            // Now we can actually resolve the Phi mappings

            Map<Integer, PhiNode.Mapping> phiMappingForSinglePred = getSinglePredSubstitution(phiNodeList, i);
            resolveSubstitutions(predBlock, phiMappingForSinglePred);
        }

    }

    /**
     * Return a map that contains the substitutions that are implied by the set of Phi nodes that are passed as a
     * list to the function. Each substitution a -> b means that when the passed predNumber is selected, the value
     * from location b is to be written to a (and will be expected there by the rest of the codebase).
     *
     * @param phiNodeList A list of Phi nodes. These must be from the same basic block and hence have the same
     *                    number of predecessors.
     * @param predNumber The number of the predecessor to calculate the substitutions for. This value must be
     *                   in the bounds of the predecessors of the Phis in the Phi node list.
     * @return A map that contains substitutions of the form a -> b, where a should be substituted by the value in
     * location b when the passed edge is used to enter the basic block the Phi nodes are in.
     */
    private static Map<Integer, PhiNode.Mapping> getSinglePredSubstitution(List<PhiNode> phiNodeList, int predNumber) {
        if (phiNodeList.isEmpty()) {
            return new HashMap<>();
        }

        if (predNumber < 0 || predNumber >= phiNodeList.get(0).getMappingCount()) {
            throw new IllegalArgumentException("Out of bounds for Phi mapping selection.");
        }

        Map<Integer, PhiNode.Mapping> result = new HashMap<>();

        for (PhiNode phiNode : phiNodeList) {
            // Left: register the source value is in
            // Right: target register for the phi node

            PhiNode.Mapping mapping = phiNode.getMappings().get(predNumber);

            result.put(mapping.getTargetRegister(), mapping);
        }

        return result;
    }

    private static void resolveSubstitutions(BasicBlock predecessorBlock, Map<Integer, PhiNode.Mapping> substitutions) {

        // Use fix-point iteration
        boolean stable;

        do {
            stable = true;

            // Sets of the registers that are used as value sources and targets for values

            // Previous value source registers
            final Set<Integer> sourceRegisters = substitutions.values()
                .stream()
                .map(PhiNode.Mapping::getSourceRegister)
                .collect(Collectors.toSet());

            // Phi target registers, create a new set instead of using the key set from the map directly
            final Set<Integer> targetRegisters = new HashSet<>(substitutions.keySet());

            // Now we need to find the targets that are not sources for values (written to, but not read from)
            targetRegisters.removeIf(sourceRegisters::contains);

            if (targetRegisters.size() > 0) {
                // We found a possible change
                stable = false;

                // Resolve all Phi substitutions that are not read from in the substitutions
                for (Integer targetRegister : targetRegisters) {

                    PhiNode.Mapping nonReadTarget = substitutions.get(targetRegister);

                    String movSuffix = nonReadTarget.getMoveSuffix();
                    String registerSuffix = nonReadTarget.getRegisterSuffix();

                    Instruction move = new GenericInstruction("mov" + movSuffix
                        + " %@" + nonReadTarget.getSourceRegister() + registerSuffix + " -> %@"
                        + nonReadTarget.getTargetRegister() + registerSuffix);

                    predecessorBlock.appendInstruction(move);

                    substitutions.remove(nonReadTarget.getTargetRegister());
                }
            }
        }
        while (!stable);

        // If any other Phi nodes are left, we need to swap them in a circular fashion as the Phis
        // include at least one cycle

        if (substitutions.size() > 0) {
            resolvePhiCycles(predecessorBlock, substitutions);
        }
    }

    private static void resolvePhiCycles(BasicBlock predecessorBlock, Map<Integer, PhiNode.Mapping> substitutions) {
        int newPhiRegister = Pseudoregister.getPhiRegisterNumber();

        if (substitutions == null) {
            return;
        }

        while (substitutions.size() > 0) {
            // Start at any point in our cycle and write the value into a temporary register
            PhiNode.Mapping firstMapping = substitutions.entrySet().iterator().next().getValue();

            String movSuffix = firstMapping.getMoveSuffix();
            String registerSuffix = firstMapping.getRegisterSuffix();

            GenericInstruction tempMove = new GenericInstruction( "mov" + movSuffix
                + " %@" + firstMapping.getTargetRegister() + registerSuffix + " -> %@"
                + newPhiRegister + registerSuffix);
            predecessorBlock.appendInstruction(tempMove);

            int nextTargetRegister = firstMapping.getTargetRegister();

            PhiNode.Mapping nextMapping = substitutions.remove(nextTargetRegister);

            while (firstMapping.getTargetRegister() != nextMapping.getSourceRegister()) {
                GenericInstruction swapMove = new GenericInstruction("mov" + movSuffix
                    + " %@" + nextMapping.getSourceRegister() + registerSuffix + " -> %@"
                    + nextMapping.getTargetRegister() + registerSuffix);
                predecessorBlock.appendInstruction(swapMove);

                nextTargetRegister = nextMapping.getSourceRegister();
                nextMapping = substitutions.remove(nextTargetRegister);
            }

            // Close the cycle again, using our new register
            GenericInstruction restoreTemp = new GenericInstruction("mov" + movSuffix
                + " %@" + newPhiRegister + registerSuffix + " -> %@"
                + nextMapping.getTargetRegister() + registerSuffix);

            predecessorBlock.appendInstruction(restoreTemp);
        }
    }
}
