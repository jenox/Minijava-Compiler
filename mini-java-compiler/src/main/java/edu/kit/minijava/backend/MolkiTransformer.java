package edu.kit.minijava.backend;

import java.util.*;

import firm.*;
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

    private Util util = new Util();

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

        this.appendMolkiCode("addl [ %@" + srcReg1 + "d | %@" + srcReg2 + "d ]" + " -> %@" + targetReg + "d");
    }

    private void molkify(Address address) {
        // nothing to do
    }

    private void molkify(Block block) {
        // nothing to do here
    }

    private void molkify(Call call) {

        Address address = (Address) call.getPred(1);
        String functionName = address.getEntity().getLdName();

        // ignore first two preds, i.e. memory and function adress
        int start = 2;

        String args = "";
        String regSuffix = "";


        // build args string
        for (int i = start; i < call.getPredCount(); i++) {
            regSuffix = this.util.mode2RegSuffix(call.getPred(i).getMode());

            int arg = this.node2RegIndex.get(call.getPred(i));
            if (call.getPredCount() - i == 1) {
                args += REG_PREFIX + arg + regSuffix;
            }
            else {
                args += REG_PREFIX + arg + regSuffix + " | ";
            }
        }


        int targetReg;


        for (BackEdges.Edge edge : BackEdges.getOuts(call)) {
            if (edge.node.getMode().equals(Mode.getT())) {
                for (BackEdges.Edge projEdge : BackEdges.getOuts(edge.node)) {
                    regSuffix = this.util.mode2RegSuffix(projEdge.node.getMode());
                }
            }
        }

        switch (functionName) {
            // TODO Are these special cases for the standard library functions required?
            case "system_out_println":
                this.appendMolkiCode("call system_out_println [ " + args + " ]");
                break;
            case "system_out_write":
                this.appendMolkiCode("call system_out_write [ " + args + " ]");
                break;
            case "system_out_flush":
                targetReg = this.node2RegIndex.get(call);
                this.appendMolkiCode("call system_out_flush [ ] -> %@" + targetReg + regSuffix);
                break;
            case "system_in_read":
                targetReg = this.node2RegIndex.get(call);
                this.appendMolkiCode("call system_in_read [ " + args + " ] -> %@" + targetReg + regSuffix);
                break;
            case "alloc_mem":
                targetReg = this.node2RegIndex.get(call);
                int srcReg1 = this.node2RegIndex.get(call.getPred(2));
                String regSuffix1 = this.util.mode2RegSuffix(call.getPred(2).getMode());
                int srcReg2 = this.node2RegIndex.get(call.getPred(3));
                String regSuffix2 = this.util.mode2RegSuffix(call.getPred(2).getMode());
                args = REG_PREFIX + srcReg1 + regSuffix1 + " | " + REG_PREFIX + srcReg2 + regSuffix2;
                this.appendMolkiCode("call alloc_mem [ " + args + " ] -> %@" + targetReg + regSuffix);
                break;
            default:
                targetReg = this.node2RegIndex.get(call);
                this.appendMolkiCode("call " + functionName + " [ " + args + " ] -> %@" + targetReg + regSuffix);
        }
    }

    private void molkify(Cmp cmp) {
        int srcReg1 = this.node2RegIndex.get(cmp.getLeft());
        int srcReg2 = this.node2RegIndex.get(cmp.getRight());

        String regSuffix = this.util.mode2RegSuffix(cmp.getLeft().getMode());
        String cmpSuffix = this.util.mode2MovSuffix(cmp.getLeft().getMode());

        this.appendMolkiCode("cmp" + cmpSuffix + " [ %@" + srcReg1 + regSuffix + " | %@" + srcReg2 + regSuffix + " ]");
    }

    private void molkify(Const aConst) {
        if (!aConst.getMode().equals(Mode.getb())) {
            String constant = CONST_PREFIX + String.valueOf(aConst.getTarval().asInt());
            int targetReg = this.node2RegIndex.get(aConst);
            String regSuffix = this.util.mode2RegSuffix(aConst.getMode());
            String movSuffix = this.util.mode2MovSuffix(aConst.getMode());

            this.appendMolkiCode("mov" + movSuffix + " " + constant + regSuffix + " -> %@" + targetReg + regSuffix);
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
        this.appendMolkiCode("divl [ %@" + left + "d | %@" + right + "d ]"
                + " -> [ %@" + targetReg1 + "d | " + REG_PREFIX + targetReg2 + "d ]");
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
        String regSuffix = this.util.mode2RegSuffix(load.getLoadMode());
        String movSuffix = this.util.mode2MovSuffix(load.getLoadMode());

        if (load.getPred(1) instanceof Sel) {
            Sel sel = (Sel) load.getPred(1);
            int baseReg = this.node2RegIndex.get(sel.getPtr());
            int indexReg = this.node2RegIndex.get(sel.getIndex());

            this.appendMolkiCode("mov" + movSuffix + " " + " (" + REG_PREFIX + baseReg + ", "
                    + REG_PREFIX + indexReg + "d" + ", " + sel.getType().getAlignment() + ")" + regSuffix
                    + " -> " + REG_PREFIX + targetReg + regSuffix);
        }
        else if (load.getPred(1) instanceof Member) {
            Member member = (Member) load.getPred(1);
            int baseReg = this.node2RegIndex.get(member.getPtr());
            int offset = member.getEntity().getOffset();

            this.appendMolkiCode("mov" + movSuffix + " " + offset + "(" + REG_PREFIX + baseReg + ")" + regSuffix
                    + " -> " + REG_PREFIX + targetReg + regSuffix);
        }
        else {
            int pointerReg = this.node2RegIndex.get(load.getPtr());

            this.appendMolkiCode("mov" + movSuffix + " " + "(" + REG_PREFIX + pointerReg + ") -> "
                    + REG_PREFIX + targetReg + regSuffix);
        }
    }

    private void molkify(Minus minus) {
        int reg = this.node2RegIndex.get(minus.getOp());
        this.appendMolkiCode("negl " + REG_PREFIX + reg + "d");
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

        this.appendMolkiCode("divl [ " + REG_PREFIX + srcReg1 + "d | " + REG_PREFIX + srcReg2 + "d ]" + " -> [ "
                + REG_PREFIX + targetReg2 + "d | " + REG_PREFIX + targetReg1 + "d ]");
    }

    private void molkify(Mul mul) {
        int srcReg1 = this.node2RegIndex.get(mul.getLeft());
        int srcReg2 = this.node2RegIndex.get(mul.getRight());

        int targetReg = this.node2RegIndex.get(mul);

        this.appendMolkiCode("mull [ " + REG_PREFIX + srcReg1 + "d | " + REG_PREFIX + srcReg2 + "d ]" + " -> [ "
                + REG_PREFIX + targetReg + "d ]");
    }

    private void molkify(Not not) {
        int reg = this.node2RegIndex.get(not.getOp());
        this.appendMolkiCode("notb " + REG_PREFIX + reg + "l");
    }

    private void molkify(Phi phi) {
        String regSuffix = this.util.mode2RegSuffix(phi.getMode());
        String movSuffix = this.util.mode2MovSuffix(phi.getMode());

        if (!phi.getMode().equals(Mode.getM())) {

            for (int i = 0; i < phi.getPredCount(); i++) {

                int regIndexOfIthPred = this.node2RegIndex.get(phi.getPred(i));
                int regIndexOfPhi = this.node2RegIndex.get(phi);
                int blockNumOfIthPred = phi.getBlock().getPred(i).getBlock().getNr();

                this.appendMolkiCode("mov" + movSuffix + " %@" + regIndexOfIthPred + regSuffix
                        + " -> %@" + regIndexOfPhi + regSuffix, blockNumOfIthPred);
            }
        }
    }

    private void molkify(Return aReturn) {

        if (aReturn.getPredCount() == 1 && !aReturn.getPred(0).getMode().equals(Mode.getM())) {
            String regSuffix = this.util.mode2RegSuffix(aReturn.getPred(0).getMode());
            String movSuffix = this.util.mode2MovSuffix(aReturn.getPred(0).getMode());
            this.appendMolkiCode("mov" + movSuffix + " %@"
                    + this.node2RegIndex.get(aReturn.getPred(0)) + regSuffix + " -> %@$" + regSuffix);
        }
        else if (aReturn.getPredCount() > 1) {
            String regSuffix = this.util.mode2RegSuffix(aReturn.getPred(1).getMode());
            String movSuffix = this.util.mode2MovSuffix(aReturn.getPred(1).getMode());
            this.appendMolkiCode("mov" + movSuffix + " %@"
                    + this.node2RegIndex.get(aReturn.getPred(1)) + regSuffix + " -> %@$" + regSuffix);
        }

        // Select single successor
        Block successorBlock = (Block) BackEdges.getOuts(aReturn).iterator().next().node;

        this.appendMolkiCode("jmp " + "L" + successorBlock.getNr());
    }

    private void molkify(Sel sel) {
    }

    private void molkify(Start start) {
        // nothing to do
    }

    private void molkify(Store store) {
        int storeReg = this.node2RegIndex.get(store.getValue());
        String regSuffix = this.util.mode2RegSuffix(store.getValue().getMode());
        String movSuffix = this.util.mode2MovSuffix(store.getValue().getMode());

        if (store.getPred(1) instanceof Sel) {
            Sel sel = (Sel) store.getPred(1);
            int baseReg = this.node2RegIndex.get(sel.getPtr());
            int indexReg = this.node2RegIndex.get(sel.getIndex());

            this.appendMolkiCode("mov" + movSuffix + " " + REG_PREFIX + storeReg + regSuffix + " -> ("
                    + REG_PREFIX + baseReg + ", " + REG_PREFIX + indexReg + "d, " + sel.getType().getAlignment() + ")" + regSuffix);
        }
        else if (store.getPred(1) instanceof Member) {
            Member member = (Member) store.getPred(1);
            int baseReg = this.node2RegIndex.get(member.getPtr());
            int offset = member.getEntity().getOffset();

            this.appendMolkiCode("mov" + movSuffix + " " + REG_PREFIX + storeReg + regSuffix + " -> "
                    + offset + "(" + REG_PREFIX + baseReg + ")" + regSuffix);
        }
        else {
            int pointerReg = this.node2RegIndex.get(store.getPtr());
            this.appendMolkiCode("mov" + movSuffix + " " + REG_PREFIX + storeReg + regSuffix + " -> ("
                    + REG_PREFIX + pointerReg + ")" + regSuffix);
        }
    }

    private void molkify(Sub sub) {
        int srcReg1 = this.node2RegIndex.get(sub.getLeft());
        int srcReg2 = this.node2RegIndex.get(sub.getRight());

        int targetReg = this.node2RegIndex.get(sub);

        String regSuffix = this.util.mode2RegSuffix(sub.getMode());

        this.appendMolkiCode("subl" + " [ " + REG_PREFIX + srcReg1 + regSuffix + " | "
                + REG_PREFIX + srcReg2 + regSuffix + " ] -> " + REG_PREFIX + targetReg + regSuffix);
    }

    private void molkify(Proj node) {
        // nothing to do
    }

    private void molkify(Cond cond) {
        Node selector = cond.getSelector();

        String condJump = null;
        String uncondJump = null;

        for (BackEdges.Edge edge : BackEdges.getOuts(cond)) {

            // Cond nodes should always be succeeded by proj nodes (with X mode set)
            Proj proj = (Proj) edge.node;

            // Get block which is the successor of the proj node
            // iterator().next() always yields the first entry in the iteration
            Block block = (Block) BackEdges.getOuts(proj).iterator().next().node;

            if (selector instanceof Cmp) {

                Cmp cmp = (Cmp) selector;
                Relation relation = cmp.getRelation();

                // Only generate a conditional jump for the true part, otherwise generate an unconditional jump
                if (proj.getNum() == Cond.pnTrue) {

                    // TODO Allow conditions to be inverted.

                    switch (relation) {
                        case Less:
                            condJump = "jl";
                            break;
                        case LessEqual:
                            condJump = "jle";
                            break;
                        case Greater:
                            condJump = "jg";
                            break;
                        case GreaterEqual:
                            condJump = "jge";
                            break;
                        case Equal:
                            condJump = "je";
                            break;
                        case LessGreater:
                        case UnorderedLessGreater:
                            condJump = "jne";
                            break;
                        default:
                            // This should not happen
                            assert false : "Unknown relation in cond node code generation!";
                            condJump = "";
                    }

                    condJump += " " + "L" + block.getNr();
                }
                else {
                    uncondJump = "jmp " + "L" + block.getNr();
                }
            }
            else {
                assert selector instanceof Const && selector.getMode().equals(Mode.getb());
                Const aConst = (Const) selector;

                if (proj.getNum() == Cond.pnTrue && aConst.getTarval().equals(TargetValue.getBTrue())) {
                    uncondJump = "jmp L" + block.getNr();
                }
                else if (proj.getNum() == Cond.pnFalse && aConst.getTarval().equals(TargetValue.getBFalse())) {
                    uncondJump = "jmp L" + block.getNr();
                }
            }

        }

        if (condJump != null) {
            this.appendMolkiCode(condJump);
        }
        if (uncondJump != null) {
            this.appendMolkiCode(uncondJump);
        }
    }

    private void molkify(Member node) {
        // nothing to do
    }

    private void molkify(Conv node) {
        // Nothing to do here as conversion nodes are only constructed for div and mod
        // operations and conversion of the operands instead should be handled there.
    }


    private void appendThreeAdressCommand(String cmd, int srcReg1, int srcReg2, int targetReg) {
        this.appendMolkiCode(cmd + " [ %@" + srcReg1 + " | %@" + srcReg2 + " ] -> %@" + targetReg);
    }
}
