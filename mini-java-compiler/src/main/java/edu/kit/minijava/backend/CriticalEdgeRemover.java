package edu.kit.minijava.backend;

import java.util.*;

public class CriticalEdgeRemover {

    public CriticalEdgeRemover() {}

    public static Map<Integer, BasicBlock> removeCriticalEdges(Map<Integer, BasicBlock> blockMap) {
        Map<Integer, BasicBlock> result = new HashMap<>();


        // Remove critical edges here!

        return result;
    }

    public static BasicBlock createNewConnectionBlock(BasicBlock pred, BasicBlock target) {
        BasicBlock connectingBlock = new BasicBlock();

        // Change control flow to the correct value here!

        return connectingBlock;
    }
}
