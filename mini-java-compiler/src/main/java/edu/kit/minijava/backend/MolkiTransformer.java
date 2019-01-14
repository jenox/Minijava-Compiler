package edu.kit.minijava.backend;

import java.util.*;

import firm.BackEdges;
import firm.Mode;
import firm.Relation;
import firm.TargetValue;
import firm.nodes.*;
import firm.nodes.NodeVisitor.Default;

public class MolkiTransformer extends Default {
    // CONSTANTS
    private static final String INDENT = "    "; // 4 spaces
    private static final String REG_PREFIX = "%@";
    private static final String CONST_PREFIX = "$";
    private int currentBlockNr;

    // ATTRIBUTES
    private HashMap<Integer, List<String>> molkiCode = new HashMap<>();

    // primarily for projections to save their register index
    private HashMap<Node, Integer> node2RegIndex;

    // GETTERS & SETTERS
    public HashMap<Integer, List<String>> getMolkiCode() {
        return this.molkiCode;
    }

    /**
     * inserts given string to ouput.
     *
     * @param molkiCode string inserted with correct indentation and linebreak at end.
     */
    private void appendMolkiCode(String molkiCode) {
        if (this.molkiCode.get(this.currentBlockNr) == null) {
            this.molkiCode.put(this.currentBlockNr, new ArrayList<>());
        }

        this.molkiCode.get(this.currentBlockNr).add(INDENT + molkiCode);
    }

    private void appendMolkiCode(String molkiCode, int blockNr) {
        this.molkiCode.get(blockNr).add(INDENT + molkiCode);
    }

    public MolkiTransformer(HashMap<Node, Integer> proj2regIndex) {
        this.node2RegIndex = proj2regIndex;
    }

    public void createValue(int blockNr, Node node) {
        this.currentBlockNr = blockNr;

        switch (node.getOpCode()) {
            case iro_Add:
                Add add = (Add) node;
                this.molkify(add);
                break;
            case iro_Sub:
                Sub sub = (Sub) node;
                this.molkify(sub);
                break;
            case iro_Mul:
                Mul mul = (Mul) node;
                this.molkify(mul);
                break;
            case iro_Div:
                Div div = (Div) node;
                this.molkify(div);
                break;
            case iro_Mod:
                Mod mod = (Mod) node;
                this.molkify(mod);
                break;
            case iro_Address:
                Address address = (Address) node;
                this.molkify(address);
                break;
            case iro_Call:
                Call call = (Call) node;
                this.molkify(call);
                break;
            case iro_Cmp:
                Cmp cmp = (Cmp) node;
                this.molkify(cmp);
                break;
            case iro_Const:
                Const aConst = (Const) node;
                this.molkify(aConst);
                break;
            case iro_End:
                End aEnd = (End) node;
                this.molkify(aEnd);
                break;
            case iro_Jmp:
                Jmp aJmp = (Jmp) node;
                this.molkify(aJmp);
                break;
            case iro_Load:
                Load aLoad = (Load) node;
                this.molkify(aLoad);
                break;
            case iro_Minus:
                Minus aMinus = (Minus) node;
                this.molkify(aMinus);
                break;
            case iro_Not:
                Not aNot = (Not) node;
                this.molkify(aNot);
                break;
            case iro_Phi:
                Phi aPhi = (Phi) node;
                this.molkify(aPhi);
                break;
            case iro_Return:
                Return aReturn = (Return) node;
                this.molkify(aReturn);
                break;
            case iro_Sel:
                Sel aSel = (Sel) node;
                this.molkify(aSel);
                break;
            case iro_Start:
                Start aStart = (Start) node;
                this.molkify(aStart);
                break;
            case iro_Store:
                Store aStore = (Store) node;
                this.molkify(aStore);
                break;
            case iro_Proj:
                Proj aProj = (Proj) node;
                this.molkify(aProj);
                break;
            case iro_Cond:
                Cond aCond = (Cond) node;
                this.molkify(aCond);
                break;
            case iro_Member:
                Member aMember = (Member) node;
                this.molkify(aMember);
                break;
            case iro_Conv:
                Conv aConv = (Conv) node;
                this.molkify(aConv);
                break;
            default:
                throw new UnsupportedOperationException("unknown node " + node.getClass());
        }
    }

    private void molkify(Add add) {
        int srcReg1 = this.node2RegIndex.get(add.getLeft());
        int srcReg2 = this.node2RegIndex.get(add.getRight());
        int targetReg = this.node2RegIndex.get(add);

        this.appendThreeAdressCommand("add", srcReg1, srcReg2, targetReg);
    }

    private void molkify(Address address) {
        // nothing to do
    }

    private void molkify(Block block) {
        // nothing to do here
    }

    private void molkify(Call call) {

        Address address = (Address) call.getPred(1);
//        String functionName = address.getEntity().getLdName().replace('.', '$');
        String functionName = address.getEntity().getLdName();

        // ignore first two preds, i.e. memory and function adress
        int start = 2;

        String args = "";

        // build args string
        for (int i = start; i < call.getPredCount(); i++) {

            int arg = this.node2RegIndex.get(call.getPred(i));
            if (call.getPredCount() - i == 1) {
                args += REG_PREFIX + arg;
            }
            else {
                args += REG_PREFIX + arg + " | ";
            }

        }

        int targetReg;

        switch (functionName) {
            // TODO Are these special cases for the standard library functions  required?
            case "system_out_println":
                this.appendMolkiCode("call system_out_println [ " + args + " ]");
                break;
            case "system_out_write":
                this.appendMolkiCode("call system_out_write [ " + args + " ]");
                break;
            case "system_out_flush":
                targetReg = this.node2RegIndex.get(call);
                this.appendMolkiCode("call system_out_flush [ ] -> %@" + targetReg);
                break;
            case "system_in_read":
                targetReg = this.node2RegIndex.get(call);
                this.appendMolkiCode("call system_in_read [ " + args + " ] -> %@" + targetReg);
                break;
            case "alloc_mem":
                targetReg = this.node2RegIndex.get(call);
                int srcReg1 = this.node2RegIndex.get(call.getPred(2));
                int srcReg2 = this.node2RegIndex.get(call.getPred(3));
                args = REG_PREFIX + srcReg1 + " | " + REG_PREFIX + srcReg2;
                this.appendMolkiCode("call alloc_mem [ " + args + " ] -> %@" + targetReg);
                break;
            default:
                targetReg = this.node2RegIndex.get(call);
                this.appendMolkiCode("call " + functionName + " [ " + args + " ] " + "-> %@" + targetReg);
        }
    }

    private void molkify(Cmp cmp) {
        int srcReg1 = this.node2RegIndex.get(cmp.getRight());
        int srcReg2 = this.node2RegIndex.get(cmp.getLeft());

        this.appendMolkiCode("cmp %@" + srcReg1 + ", %@" + srcReg2);
    }

    private void molkify(Const aConst) {
        if (!aConst.getMode().equals(Mode.getb())) {
            String constant = CONST_PREFIX + String.valueOf(aConst.getTarval().asInt());
            int targetReg = this.node2RegIndex.get(aConst);

            this.appendMolkiCode("mov " + constant + ", %@" + targetReg);
        }
    }

    private void molkify(Div div) {
        int left = this.node2RegIndex.get(div.getLeft());
        int right = this.node2RegIndex.get(div.getRight());

        int targetReg1 = this.node2RegIndex.get(div);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor

        // TODO Div instruction has to handle sign extension of the operands.
        // but this requires knowledge of the length of used register sizes for virtual registers.

        // Reference code that should be generated for div instruction:
        // mov %edi, %eax
        // movslq %eax, %rax
        // cqto
        // movslq %esi, %rsi
        // idivq %rsi
        this.appendMolkiCode("idiv [ %@" + left + " | %@" + right + " ]" + " -> [ %@" + targetReg1 + " | " + REG_PREFIX
                        + targetReg2 + "]");
    }

    private void molkify(End end) {
        // nothing to do
    }

    private void molkify(Jmp jmp) {
        int numberOfSuccessors = 0;

        for (BackEdges.Edge edge : BackEdges.getOuts(jmp)) {
            // a jmp should never be connected to more than one successor block
            numberOfSuccessors++;
            assert numberOfSuccessors < 2;

            Block block = (Block) edge.node;
            this.appendMolkiCode("jmp L" + block.getNr());
        }
    }



    private void molkify(Load load) {
        int targetReg = this.node2RegIndex.get(load);

        if (load.getPred(1) instanceof Sel) {
            Sel sel = (Sel) load.getPred(1);
            int baseReg = this.node2RegIndex.get(sel.getPtr());
            int indexReg = this.node2RegIndex.get(sel.getIndex());

            this.appendMolkiCode(
                    "mov (" + REG_PREFIX + baseReg  + ", "
                            + REG_PREFIX + indexReg + ", "
                            + sel.getType().getAlignment() + "), " + REG_PREFIX + targetReg);
        }
        else if (load.getPred(1) instanceof Member) {
            Member member = (Member) load.getPred(1);
            int baseReg = this.node2RegIndex.get(member.getPtr());
            int offset = member.getEntity().getOffset();

            this.appendMolkiCode("mov " + offset + "(" + REG_PREFIX + baseReg + "), " + REG_PREFIX + targetReg);
        }
        else {
            int pointerReg = this.node2RegIndex.get(load.getPtr());

            this.appendMolkiCode("mov " + "(" + REG_PREFIX + pointerReg + "), " + REG_PREFIX + targetReg);
        }
    }

    private void molkify(Minus minus) {
        int reg = this.node2RegIndex.get(minus.getOp());
        this.appendMolkiCode("neg " + REG_PREFIX + reg);
    }

    private void molkify(Mod mod) {
        int srcReg1 = this.node2RegIndex.get(mod.getLeft());
        int srcReg2 = this.node2RegIndex.get(mod.getRight());

        int targetReg1 = this.node2RegIndex.get(mod);
        int targetReg2 = targetReg1 + 1; // by convention used in PrepVisitor

        // TODO Div instruction has to handle sign extension of the operands.
        // but this requires knowledge of the length of used register sizes for virtual registers.

        // Reference code that should be generated for div instruction:
        // mov %edi, %eax
        // movslq %eax, %rax
        // cqto
        // movslq %esi, %rsi
        // idivq %rsi

        this.appendMolkiCode("idiv [ " + REG_PREFIX + srcReg1 + " | " + REG_PREFIX + srcReg2 + " ]" + " -> [ "
                        + REG_PREFIX + targetReg2 + " | " + REG_PREFIX + targetReg1 + "]");
    }

    private void molkify(Mul mul) {
        int srcReg1 = this.node2RegIndex.get(mul.getLeft());
        int srcReg2 = this.node2RegIndex.get(mul.getRight());

        int targetReg = this.node2RegIndex.get(mul);

        this.appendThreeAdressCommand("imul", srcReg1, srcReg2, targetReg);
    }

    private void molkify(Not not) {
        int reg = this.node2RegIndex.get(not.getOp());
        this.appendMolkiCode("not " + REG_PREFIX + reg);
    }

    private void molkify(Phi phi) {
        if (!phi.getMode().equals(Mode.getM())) {

            for (int i = 0; i < phi.getPredCount(); i++) {
                this.appendMolkiCode("mov %@" + this.node2RegIndex.get(phi.getPred(i)) + ", %@"
                    + this.node2RegIndex.get(phi), phi.getBlock().getPred(i).getBlock().getNr());
            }
        }
    }

    private void molkify(Return aReturn) {
        if (aReturn.getPredCount() == 1 && !aReturn.getPred(0).getMode().equals(Mode.getM())) {
            this.appendMolkiCode("mov %@" + this.node2RegIndex.get(aReturn.getPred(0)) + ", %@r0");
        }
        else if (aReturn.getPredCount() > 1) {
            this.appendMolkiCode("mov %@" + this.node2RegIndex.get(aReturn.getPred(1)) + ", %@r0");
        }
    }

    private void molkify(Sel sel) {
    }

    private void molkify(Start start) {
        //nothing to do
    }

    private void molkify(Store store) {
        int storeReg = this.node2RegIndex.get(store.getValue());

        if (store.getPred(1) instanceof Sel) {
            Sel sel = (Sel) store.getPred(1);
            int baseReg = this.node2RegIndex.get(sel.getPtr());
            int indexReg = this.node2RegIndex.get(sel.getIndex());

            this.appendMolkiCode("mov " + REG_PREFIX + storeReg
                    + ", (" + REG_PREFIX + baseReg + ", "
                    + REG_PREFIX + indexReg + ", "
                    + sel.getType().getAlignment() + ")");
        }
        else if (store.getPred(1) instanceof Member) {
            Member member = (Member) store.getPred(1);
            int baseReg = this.node2RegIndex.get(member.getPtr());
            int offset = member.getEntity().getOffset();

            this.appendMolkiCode("mov " + REG_PREFIX + storeReg + ", " + offset + "(" + REG_PREFIX + baseReg + ")");
        }
        else {
            int pointerReg = this.node2RegIndex.get(store.getPtr());
            this.appendMolkiCode("mov " + REG_PREFIX + storeReg + ", (" + REG_PREFIX + pointerReg + ")");
        }
    }

    private void molkify(Sub sub) {
        int srcReg1 = this.node2RegIndex.get(sub.getRight());
        int srcReg2 = this.node2RegIndex.get(sub.getLeft());

        int targetReg = this.node2RegIndex.get(sub);

        this.appendThreeAdressCommand("sub", srcReg1, srcReg2, targetReg);
    }

    private void molkify(Proj node) {
        //nothing to do
    }

    private void molkify(Cond cond) {
        // jmp asm
        int succBlockNr = -1;
        boolean hasReturn = false;
        boolean hasOnlyMemPred = false;

        List<Integer> blockNrs = new ArrayList<>();
        List<Integer> projNrs = new ArrayList<>();
        List<Integer> registerNrs = new ArrayList<>();

        // get projections out of Condition
        for (BackEdges.Edge condEdge : BackEdges.getOuts(cond)) {
            // get Blocks
            for (BackEdges.Edge projEdge : BackEdges.getOuts(condEdge.node)) {
                Block block = (Block) projEdge.node;

                // get first node of block
                for (BackEdges.Edge blockEdge : BackEdges.getOuts(block)) {
                    if (blockEdge.node instanceof Return) {
                        Return aReturn = (Return) blockEdge.node;
                        hasReturn = true;
                        hasOnlyMemPred = aReturn.getPredCount() <= 1;

                        if (!hasOnlyMemPred) {
                            succBlockNr = block.getGraph().getEndBlock().getNr();
                            registerNrs.add(this.node2RegIndex.get(aReturn.getPred(1)));
                        }
                    }
                }
                blockNrs.add(block.getNr());
                projNrs.add(condEdge.node.getNr());
            }
        }

        // sometimes, firm switches the position of the proj nodes
        boolean projAreReversed = projNrs.get(0) > projNrs.get(1);
        boolean blocksReversed = blockNrs.get(0) < blockNrs.get(1);
        int trueNr = projAreReversed ? 1 : 0;
        int falseNr = projAreReversed ? 0 : 1;

        if (!hasReturn) {
            succBlockNr = blocksReversed ? blockNrs.get(falseNr) : blockNrs.get(trueNr);
        }
        else if (!hasOnlyMemPred) {
            this.appendMolkiCode("mov %@" + registerNrs.get(trueNr) + ", %@r0");
            blocksReversed = false;
        }
        else {
            succBlockNr = blockNrs.get(trueNr);
        }

        Node selector = cond.getSelector();

        if (selector instanceof Cmp) {
            Cmp cmp = (Cmp) selector;

            if (cmp.getRelation().equals(Relation.Less)) {
                String jmp = blocksReversed ? "jge" : "jl";
                this.appendMolkiCode(jmp + " L" + succBlockNr);
            }
            else if (cmp.getRelation().equals(Relation.LessEqual)) {
                String jmp = blocksReversed ? "jg" : "jle";
                this.appendMolkiCode(jmp + " L" + succBlockNr);
            }
            else if (cmp.getRelation().equals(Relation.Greater)) {
                String jmp = blocksReversed ? "jle" : "jg";
                this.appendMolkiCode(jmp + " L" + succBlockNr);
            }
            else if (cmp.getRelation().equals(Relation.GreaterEqual)) {
                String jmp = blocksReversed ? "jl" : "jge";
                this.appendMolkiCode(jmp + " L" + succBlockNr);
            }
            else if (cmp.getRelation().equals(Relation.Equal)) {
                String jmp = blocksReversed ? "jne" : "je";
                this.appendMolkiCode(jmp + " L" + succBlockNr);
            }
            else if (cmp.getRelation().equals(Relation.Equal.negated())) {
                String jmp = blocksReversed ? "je" : "jne";
                this.appendMolkiCode(jmp + " L" + succBlockNr);
            }
        }
        // in all other cases we should be a boolean constant
        else {
            assert selector instanceof Const && selector.getMode().equals(Mode.getb());
            Const aConst = (Const) selector;

            if (aConst.getTarval().equals(TargetValue.getBTrue())) {
                this.appendMolkiCode("jmp L" + blockNrs.get(trueNr));
            }
            else {
                this.appendMolkiCode("jmp L" + blockNrs.get(falseNr));
            }
        }
    }

    private void molkify(Member node) {
        //nothing to do
    }

    private void molkify(Conv node) {
        // Nothing to do here as conversion nodes are only constructed for div and mod
        // operations and conversion of the operands instead should be handled there.
    }

    /*
     * UTILITY FUNCTIONS
     */

    private void appendThreeAdressCommand(String cmd, int srcReg1, int srcReg2, int targetReg) {
        this.appendMolkiCode(cmd + " [ %@" + srcReg1 + " | %@" + srcReg2 + " ] -> %@" + targetReg);
    }
}
