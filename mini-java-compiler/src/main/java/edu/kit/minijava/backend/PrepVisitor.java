package edu.kit.minijava.backend;

import java.util.*;

import firm.Mode;
import firm.nodes.*;
import firm.nodes.NodeVisitor.Default;

public class PrepVisitor extends Default {
    // ATTRIBUTES
    // TODO: handle different methods (different methods could use the same block numbers)
    // brauchen wir eigentlich nicht
    private int registerIndex = 0;

    private HashMap<Node, String> jmp2BlockName = new HashMap<>();
    private HashMap<Address, String> ptr2Name = new HashMap<>();
    private HashMap<Node, Integer> node2regIndex = new HashMap<>();
    private HashMap<Node, List<Integer>> nodeToPhiReg = new HashMap<>();

    // GETTERS
    public HashMap<Node, String> getJmp2BlockName() {
        return this.jmp2BlockName;
    }

    public HashMap<Node, Integer> getProj2regIndex() {
        return this.node2regIndex;
    }

    public HashMap<Node, List<Integer>> getBlockToPhiReg() {
        return this.nodeToPhiReg;
    }

    @Override
    public void visit(Add add) {
        int currentIndex = this.registerIndex++;
        this.node2regIndex.put(add, currentIndex);
    }

    @Override
    public void visit(Address address) {
        this.ptr2Name.put(address, address.getEntity().getName());
    }

    @Override
    public void visit(Block block) {
        if (block.getPredCount() > 0) {
            String name = "L" + block.getNr();

            // this is also adding non-Jmp nodes to the hashmap,
            // which is ok, because only jmp nodes should use the hashmap
            block.getPreds().forEach(p -> this.jmp2BlockName.put(p, name));
        }
    }

    @Override
    public void visit(Call call) {
        // allocate register for call, regardless of whether is is used or not
        this.node2regIndex.put(call, this.registerIndex);
        this.registerIndex++;
    }

    @Override
    public void visit(Cmp cmp) {
        // nothing to do
    }

    @Override
    public void visit(Const aConst) {
        this.node2regIndex.put(aConst, this.registerIndex++);
    }

    @Override
    public void visit(Div div) {
        // we need two target registers for div. These are registerIndex and registerIndex + 1.
        // Assignment of registerIndex+1 is not represented explicitly.
        this.node2regIndex.put(div, this.registerIndex);

        this.registerIndex += 2;
    }

    @Override
    public void visit(Mod mod) {
        // we need two target registers for mod. These are registerIndex and registerIndex + 1.
        // Assignment of registerIndex+1 is not represented explicitly.
        this.node2regIndex.put(mod, this.registerIndex);

        this.registerIndex += 2;

    }

    @Override
    public void visit(Phi phi) {
        for (Node node : phi.getPreds()) {
            this.appendList(node, this.registerIndex);
        }

        this.registerIndex++;

    }

    @Override
    public void visit(Proj proj) {
        // this projection is parameter if predecessor is args node
        boolean isParam = proj.getGraph().getArgs().equals(proj.getPred());
        int register = 0;
        if (isParam) {
            register = proj.getNum();
        }
        else {
            Iterable<Node> watchPred = proj.getPreds();
            Node node = proj.getBlock();
            int nr = proj.getNr();
            Node pred = proj.getPred();
            if (pred.getPredCount() > 0 && !proj.getMode().equals(Mode.getX())
                            && !proj.getMode().equals(Mode.getT())
                            && !proj.getMode().equals(Mode.getM())) {
                register = this.node2regIndex.get(pred.getPred(0));
            }
            else {
                return;
            }
        }

        this.node2regIndex.put(proj, register);
    }

    @Override
    public void visit(Return aReturn) {
        this.node2regIndex.put(aReturn, this.registerIndex++);
    }

    @Override
    public void visit(Sel sel) {
        this.node2regIndex.put(sel, this.registerIndex++);
    }

    @Override
    public void visit(Sub sub) {
        this.node2regIndex.put(sub, this.registerIndex++);
    }

    /**
     * add mapping from node to register of phi instruction
     *
     * @param node node to set target register for
     * @param reg  register
     */
    private void appendList(Node node, int reg) {
        List<Integer> regs = this.nodeToPhiReg.get(node);
        if (regs == null) {
            regs = new ArrayList<>();
            this.nodeToPhiReg.put(node, regs);
        }

        regs.add(reg);
    }

    @Override
    public void visit(Start node) {
        // nothing to do
    }

    @Override
    public void visit(Cond node) {
        // nothing to do
    }

    @Override
    public void visit(End node) {
        // nothing to do
    }

    @Override
    public void defaultVisit(Node n) {
        throw new UnsupportedOperationException("unknown node: " + n + " " + n.getClass());
    }

    public void setRegisterIndex(int registerIndex) {
        this.registerIndex = registerIndex;
    }

}
