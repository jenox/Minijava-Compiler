package edu.kit.minijava.backend;

import java.util.*;

import firm.nodes.*;
import firm.nodes.NodeVisitor.Default;


public class PrepVisitor extends Default {
    // ATTRIBUTES
    // TODO: handle different methods (different methods could use the same block numbers)
    //brauchen wir eigentlich nicht
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

    public HashMap<Node,List<Integer>> getBlockToPhiReg() {
        return this.nodeToPhiReg;
    }

    @Override
    public void visit(Add add) {
        add.getLeft().accept(this);
        add.getRight().accept(this);

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
        call.getPtr().accept(this);

        //allocate register for call, regardless of whether is is used or not
        this.node2regIndex.put(call, this.registerIndex);
        this.registerIndex++;
    }

    @Override
    public void visit(Cmp cmp) {
        // TODO: shouldn't there also be a jmp being generated here?
        //eigentlich muss hier nichts getan werden
        cmp.getLeft().accept(this);
        cmp.getRight().accept(this);
    }

    @Override
    public void visit(Const aConst) {
        this.node2regIndex.put(aConst, this.registerIndex++);
    }

    @Override
    public void visit(Div div) {
        div.getLeft().accept(this);
        div.getRight().accept(this);

        //we need two target registers for div. These are registerIndex and registerIndex + 1.
        //Assignment of registerIndex+1 is not represented explicitly.
        this.node2regIndex.put(div, this.registerIndex);

        this.registerIndex += 2;
    }

    @Override
    public void visit(Mod mod) {
        mod.getLeft().accept(this);
        mod.getRight().accept(this);

        //we need two target registers for mod. These are registerIndex and registerIndex + 1.
        //Assignment of registerIndex+1 is not represented explicitly.
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
        this.node2regIndex.put(proj, this.registerIndex++);
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
        sub.getLeft().accept(this);
        sub.getRight().accept(this);

        this.node2regIndex.put(sub, this.registerIndex++);
    }

    /**
     * add mapping from node to register of phi instruction
     * @param node node to set target register for
     * @param reg register
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
        //nothing to do
    }

    @Override
    public void visit(Cond node) {
        //nothing to do
    }

    @Override
    public void visit(End node) {
        //nothing to do
    }


    @Override
    public void defaultVisit(Node n) {
        throw new UnsupportedOperationException("unknown node: " + n + " " + n.getClass());
    }

}
