package edu.kit.minijava.backend;

import firm.*;
import firm.nodes.*;
import java.util.*;

public class MolkiVisitor implements NodeVisitor {
    // CONSTANTS
    private String newlineCmd = "\n    ";

    // ATTRIBUTES
    private String molkiCode = "";
    private int registerIndex = 0;

    private String tempResult = "";

    private HashMap<Address, String> ptr2Name = new HashMap<>();
    private HashMap<Node, String> jmp2BlockName = new HashMap<>();


    // GETTERS & SETTERS
    public String getMolkiCode() {
        return this.molkiCode;
    }

    public void setMolkiCode(String molkiCode) {
        this.molkiCode = molkiCode;
    }

    public void appendMolkiCode(String molkiCode) {
        this.molkiCode += molkiCode;
    }

    // METHODS
    @Override
    public void visit(Add add) {
        add.getLeft().accept(this);
        String left = this.tempResult;
        add.getRight().accept(this);
        String right = this.tempResult;

        int currentIndex = this.registerIndex++;

        this.appendMolkiCode(this.newlineCmd + "add [ " + left + " | " + right + " ] -> %@" + currentIndex);
        this.tempResult = "%@" + currentIndex;
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
        // TODO: this isn't working, how to do this better?
        if (block.getPredCount() > 0) {
            String name = "Block_" + block.getNr();
            this.appendMolkiCode("\n" + name + ":");

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

        // TODO: check, if this works as intended
        Iterator<Node> iter = call.getPreds().iterator();
        ArrayList<Node> parameters = new ArrayList<>();

        while (iter.hasNext()) {
            parameters.add(iter.next());
        }

        parameters.remove(0); // memory / side effect
        parameters.remove(0); // function address
        parameters.remove(0); // object pointer

        String args = "";
        for (int i = 0; i < parameters.size(); i++) {
            parameters.get(i).accept(this);

            if (parameters.size() - i == 1) {
                args += this.tempResult;
            }
            else {
                args += this.tempResult + " | ";
            }
        }

        int currentIndex = this.registerIndex++;
        this.appendMolkiCode(this.newlineCmd + "call " + functionName + " [ " + args + " ] -> %@" + currentIndex);
        this.tempResult = "%@" + currentIndex;

        // TODO: handle calling of runtime functions
    }

    @Override
    public void visit(Cmp cmp) {
        // TODO: shouldn't there also be a jmp being generated here?
        cmp.getLeft().accept(this);
        String left = this.tempResult;
        cmp.getRight().accept(this);
        String right = this.tempResult;

        // TODOgg
        this.appendMolkiCode(this.newlineCmd + "cmp " + left + ", " + right);
    }

    @Override
    public void visit(Cond cond) {
        // TODO: what to do here?
    }

    @Override
    public void visit(Confirm confirm) {

    }

    @Override
    public void visit(Const aConst) {
        this.tempResult = "$" + String.valueOf(aConst.getTarval().asInt());
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
        String left = this.tempResult;
        div.getRight().accept(this);
        String right = this.tempResult;

        // TODO: how to handle div results properly?
        this.appendMolkiCode(this.newlineCmd + "idiv [ " + left + " | " + right + " ]"
                           + " -> [ %@" + this.registerIndex++ + " | %@" + this.registerIndex++ + "]");
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
        String name = this.jmp2BlockName.get(jmp);
        this.appendMolkiCode(this.newlineCmd + "jmp " + name);
    }

    @Override
    public void visit(Load load) {
        load.getPtr().accept(this);
        String ptrResult = this.tempResult;

        int currentIndex = this.registerIndex++;

        this.appendMolkiCode(this.newlineCmd + "mov " + "(" + ptrResult + "), %@" + currentIndex);
        this.tempResult = "%@" + currentIndex;
    }

    @Override
    public void visit(Member member) {

    }

    @Override
    public void visit(Minus minus) {
        minus.getOp().accept(this);
        this.appendMolkiCode( this.newlineCmd + "neg " + tempResult);
    }

    @Override
    public void visit(Mod mod) {
        mod.getLeft().accept(this);
        String left = this.tempResult;
        mod.getRight().accept(this);
        String right = this.tempResult;

        // TODO: how to handle mod results properly?
        this.appendMolkiCode(this.newlineCmd + "imod [ " + left + " | " + right + " ]"
                           + " -> [ %@" + this.registerIndex++ + " | %@" + this.registerIndex++ + "]");
    }

    @Override
    public void visit(Mul mul) {
        mul.getLeft().accept(this);
        String left = this.tempResult;
        mul.getRight().accept(this);
        String right = this.tempResult;

        int currentIndex = this.registerIndex++;

        this.appendMolkiCode(this.newlineCmd + "mul [ " + left + " | " + right + " ] -> %@" + currentIndex);
        this.tempResult = "%@" + currentIndex;
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
        not.getOp().accept(this);
        this.appendMolkiCode(this.newlineCmd + "not " + tempResult);
    }

    @Override
    public void visit(Offset offset) {

    }

    @Override
    public void visit(Or or) {

    }

    @Override
    public void visit(Phi phi) {
        // TODO: what to do here?
    }

    @Override
    public void visit(Pin pin) {

    }

    @Override
    public void visit(Proj proj) {
        this.tempResult = "%@0";
        if (proj.getPred() instanceof Call) {
            // TODO: how to set correct register number here?
            //Node arg = proj.getPred().getPred(proj.getNum());
            //arg.accept(this);
        }
    }

    @Override
    public void visit(Raise raise) {

    }

    @Override
    public void visit(Return aReturn) {

    }

    @Override
    public void visit(Sel sel) {
        sel.getPtr().accept(this);
        String ptrResult = this.tempResult;
        sel.getIndex().accept(this);
        String indexResult = this.tempResult;

        int currentIndex = this.registerIndex++;

        this.appendMolkiCode(this.newlineCmd + "mov " + indexResult + "(" + ptrResult + "), %@" + currentIndex);
        this.tempResult = "%@" + currentIndex;
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
        String value = this.tempResult;
        store.getPtr().accept(this);
        String ptrResult = this.tempResult;

        int currentIndex = this.registerIndex++;

        this.appendMolkiCode(this.newlineCmd + "mov " + value + ", (" + ptrResult + ")");
        this.tempResult = "%@" + currentIndex;
    }

    @Override
    public void visit(Sub sub) {
        sub.getLeft().accept(this);
        String left = this.tempResult;
        sub.getRight().accept(this);
        String right = this.tempResult;

        int currentIndex = this.registerIndex++;

        // RIGHT AND LEFT ARE SWITCHED BECAUSE OF ASSEMBLER SYNTAX
        this.appendMolkiCode( this.newlineCmd + "sub [ " + right + " | " + left + " ] -> %@" + currentIndex);
        this.tempResult = "%@" + currentIndex;
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
