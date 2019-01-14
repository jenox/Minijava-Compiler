package edu.kit.minijava.backend;

import java.util.*;

import firm.Graph;
import firm.Mode;
import firm.nodes.*;
import firm.nodes.NodeVisitor.Default;

public class PrepVisitor extends Default {
    // ATTRIBUTES
    private int registerIndex = 0;
    private HashMap<Node, Integer> node2regIndex = new HashMap<>();
    private HashMap<Integer, List<Node>> blockId2Nodes = new HashMap<>();
    private HashMap<Graph, List<Integer>> graph2BlockId = new HashMap<>();

    // GETTERS
    public HashMap<Node, Integer> getNode2RegIndex() {
        return this.node2regIndex;
    }
    public HashMap<Integer, List<Node>> getBlockId2Nodes() {
        return this.blockId2Nodes;
    }
    public HashMap<Graph, List<Integer>> getGraph2BlockId() {
        return this.graph2BlockId;
    }

    @Override
    public void visit(Add add) {
        int currentIndex = this.registerIndex++;
        this.node2regIndex.put(add, currentIndex);

        this.addInstrToBlock(null, add);
    }

    @Override
    public void visit(Address address) {
        // nothing to do
    }

    @Override
    public void visit(Block block) {
        if (this.graph2BlockId.get(block.getGraph()) == null) {
            this.graph2BlockId.put(block.getGraph(), new ArrayList<>());
        }
        this.graph2BlockId.get(block.getGraph()).add(block.getNr());

        if (this.blockId2Nodes.get(block.getNr()) == null) {
            this.blockId2Nodes.put(block.getNr(), new ArrayList<>());
        }
    }

    @Override
    public void visit(Call call) {
        // allocate register for call, regardless of whether is is used or not
        this.node2regIndex.put(call, this.registerIndex++);

        this.addInstrToBlock(null, call);
    }

    @Override
    public void visit(Cmp cmp) {
        this.addInstrToBlock(null, cmp);
    }

    @Override
    public void visit(Const aConst) {
        this.node2regIndex.put(aConst, this.registerIndex++);

        this.addInstrToBlock(null, aConst);
    }

    @Override
    public void visit(Div div) {
        // we need two target registers for div. These are registerIndex and registerIndex + 1.
        // Assignment of registerIndex+1 is not represented explicitly.
        this.node2regIndex.put(div, this.registerIndex);

        this.registerIndex += 2;

        this.addInstrToBlock(null, div);
    }

    @Override
    public void visit(Mod mod) {
        // we need two target registers for mod. These are registerIndex and registerIndex + 1.
        // Assignment of registerIndex+1 is not represented explicitly.
        this.node2regIndex.put(mod, this.registerIndex);

        this.registerIndex += 2;

        this.addInstrToBlock(null, mod);
    }

    @Override
    public void visit(Mul node) {
        this.node2regIndex.put(node, this.registerIndex++);
        this.addInstrToBlock(null, node);
    }

    @Override
    public void visit(Minus node) {
        this.node2regIndex.put(node, this.registerIndex++);
        this.addInstrToBlock(null, node);
    }


    @Override
    public void visit(Phi phi) {
        this.node2regIndex.put(phi, this.registerIndex++);

        this.addInstrToBlock(null, phi);
    }

    @Override
    public void visit(Proj proj) {
        // this projection is a parameter if its predecessor is an args node
        boolean isParam = proj.getPred().equals(proj.getGraph().getArgs());
        int register = 0;

        if (isParam) {
            register = proj.getNum();
        }
        else if (!proj.getMode().equals(Mode.getM()) && !proj.getMode().equals(Mode.getX())) {
            Node pred = proj.getPred();

            if (!pred.equals(proj.getGraph().getStart())) {
                register = this.node2regIndex.get(pred);
            }
        }

        // the projection points to the register of its predecessor
        this.node2regIndex.put(proj, register);

        this.addInstrToBlock(null, proj);
    }

    @Override
    public void visit(Return aReturn) {
        this.node2regIndex.put(aReturn, this.registerIndex++);

        this.addInstrToBlock(null, aReturn);
    }

    @Override
    public void visit(Sel sel) {
        this.node2regIndex.put(sel, this.registerIndex++);

        this.addInstrToBlock(null, sel);
    }

    @Override
    public void visit(Sub sub) {
        this.node2regIndex.put(sub, this.registerIndex++);

        this.addInstrToBlock(null, sub);
    }

    @Override
    public void visit(Start node) {
        // nothing to do
    }

    @Override
    public void visit(Cond node) {
        this.addInstrToBlock(null, node);
    }

    @Override
    public void visit(Conv node) {
        Integer operandRegisterIndex = this.node2regIndex.get(node.getOp());
        assert operandRegisterIndex != null : "No register index set for previous node in conv node!";

        this.node2regIndex.put(node, operandRegisterIndex);
    }

    @Override
    public void visit(End node) {
        // nothing to do
    }

    @Override
    public void visit(Jmp node) {
        this.addInstrToBlock(null, node);
    }

    @Override
    public void visit(Member node) {
        // nothing to do
    }

    @Override
    public void visit(Load node) {
        this.node2regIndex.put(node, this.registerIndex++);

        this.addInstrToBlock(null, node);
    }

    @Override
    public void visit(Not not) {
        int reg = this.node2regIndex.get(not.getOp());
        this.node2regIndex.put(not, reg);

        this.addInstrToBlock(null, not);
    }

    @Override
    public void visit(Store store) {
        this.node2regIndex.put(store, this.registerIndex++);

        this.addInstrToBlock(null, store);
    }

    @Override
    public void defaultVisit(Node n) {
        throw new UnsupportedOperationException("unknown node: " + n + " " + n.getClass() + "\tmode " + n.getMode());
    }

    public void setRegisterIndex(int registerIndex) {
        this.registerIndex = registerIndex;
    }

    private void addInstrToBlock(Integer blockNr, Node node) {
        int temp = blockNr == null ? node.getBlock().getNr() : blockNr;

        if (this.blockId2Nodes.get(temp) == null) {
            this.blockId2Nodes.put(temp, new ArrayList<>());
        }
        this.blockId2Nodes.get(temp).add(node);
    }
}
