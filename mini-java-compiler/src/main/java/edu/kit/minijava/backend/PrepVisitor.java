package edu.kit.minijava.backend;

import firm.*;
import firm.nodes.*;

import java.util.*;


public class PrepVisitor implements NodeVisitor {
    // ATTRIBUTES
    // TODO: handle different methods (different methods could use the same block numbers)
    private int registerIndex = 0;

    private HashMap<Node, String> jmp2BlockName = new HashMap<>();
    private HashMap<Address, String> ptr2Name = new HashMap<>();
    private HashMap<Node, Integer> proj2regIndex = new HashMap<>();

    // GETTERS
    public HashMap<Node, String> getJmp2BlockName() {
        return this.jmp2BlockName;
    }

    public HashMap<Node, Integer> getProj2regIndex() {
        return this.proj2regIndex;
    }

    @Override
    public void visit(Add add) {
        add.getLeft().accept(this);
        add.getRight().accept(this);

        int currentIndex = this.registerIndex++;
        this.proj2regIndex.put(add, currentIndex);
    }

    @Override
    public void visit(Address address) {
        this.ptr2Name.put(address, address.getEntity().getName());
    }

    @Override
    public void visit(Align align) {

    }

    @Override
    public void visit(Alloc alloc) {

    }

    @Override
    public void visit(Anchor anchor) {

    }

    @Override
    public void visit(And and) {

    }

    @Override
    public void visit(Bad bad) {

    }

    @Override
    public void visit(Bitcast bitcast) {

    }

    @Override
    public void visit(Block block) {
        if (block.getPredCount() > 0) {
            String name = "Block_" + block.getNr();

            // this is also adding non-Jmp nodes to the hashmap,
            // which is ok, because only jmp nodes should use the hashmap
            block.getPreds().forEach(p -> this.jmp2BlockName.put(p, name));
        }
    }

    @Override
    public void visit(Builtin builtin) {

    }

    @Override
    public void visit(Call call) {
        call.getPtr().accept(this);

        String functionName = this.ptr2Name.get(call.getPtr());

        // TODO: should this distinction be made, or should we just always have a register for a call?
        switch (functionName) {
            case "system_out_println":
                break;
            case "system_out_write":
                break;
            case "system_out_flush":
                this.proj2regIndex.put(call, this.registerIndex++);
                break;
            case "system_in_read":
                this.proj2regIndex.put(call, this.registerIndex++);
                break;
            case "alloc_mem":
                this.proj2regIndex.put(call, this.registerIndex++);
                break;
            default:
                this.proj2regIndex.put(call, this.registerIndex++);
        }
    }

    @Override
    public void visit(Cmp cmp) {
        // TODO: shouldn't there also be a jmp being generated here?
        cmp.getLeft().accept(this);
        cmp.getRight().accept(this);
    }

    @Override
    public void visit(Cond cond) {

    }

    @Override
    public void visit(Confirm confirm) {

    }

    @Override
    public void visit(Const aConst) {
        this.proj2regIndex.put(aConst, this.registerIndex++);
    }

    @Override
    public void visit(Conv conv) {

    }

    @Override
    public void visit(CopyB copyB) {

    }

    @Override
    public void visit(Deleted deleted) {

    }

    @Override
    public void visit(Div div) {
        div.getLeft().accept(this);
        div.getRight().accept(this);

        this.proj2regIndex.put(div, this.registerIndex++); //TODO: 2 Zielregister
    }

    @Override
    public void visit(Dummy dummy) {

    }

    @Override
    public void visit(End end) {

    }

    @Override
    public void visit(Eor eor) {

    }

    @Override
    public void visit(Free free) {

    }

    @Override
    public void visit(IJmp iJmp) {

    }

    @Override
    public void visit(Id id) {

    }

    @Override
    public void visit(Jmp jmp) {

    }

    @Override
    public void visit(Load load) {

    }

    @Override
    public void visit(Member member) {

    }

    @Override
    public void visit(Minus minus) {

    }

    @Override
    public void visit(Mod mod) {
        //TODO: wie bei Div
    }

    @Override
    public void visit(Mul mul) {

    }

    @Override
    public void visit(Mulh mulh) {

    }

    @Override
    public void visit(Mux mux) {

    }

    @Override
    public void visit(NoMem noMem) {

    }

    @Override
    public void visit(Not not) {

    }

    @Override
    public void visit(Offset offset) {

    }

    @Override
    public void visit(Or or) {

    }

    @Override
    public void visit(Phi phi) {
        //TODO: kompliziert
    }

    @Override
    public void visit(Pin pin) {

    }

    @Override
    public void visit(Proj proj) {
        this.proj2regIndex.put(proj, this.registerIndex++);
    }

    @Override
    public void visit(Raise raise) {

    }

    @Override
    public void visit(Return aReturn) {
        this.proj2regIndex.put(aReturn, this.registerIndex++);
    }

    @Override
    public void visit(Sel sel) {
        //TODO
        this.proj2regIndex.put(sel, this.registerIndex++);
    }

    @Override
    public void visit(Shl shl) {

    }

    @Override
    public void visit(Shr shr) {

    }

    @Override
    public void visit(Shrs shrs) {

    }

    @Override
    public void visit(Size size) {

    }

    @Override
    public void visit(Start start) {

    }

    @Override
    public void visit(Store store) {

    }

    @Override
    public void visit(Sub sub) {
        sub.getLeft().accept(this);
        sub.getRight().accept(this);

        this.proj2regIndex.put(sub, this.registerIndex++);
    }

    @Override
    public void visit(Switch aSwitch) {

    }

    @Override
    public void visit(Sync sync) {

    }

    @Override
    public void visit(Tuple tuple) {

    }

    @Override
    public void visit(Unknown unknown) {

    }

    @Override
    public void visitUnknown(Node node) {

    }
}
