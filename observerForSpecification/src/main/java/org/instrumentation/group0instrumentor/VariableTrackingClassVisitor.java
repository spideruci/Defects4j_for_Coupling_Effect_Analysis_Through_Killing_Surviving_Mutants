package org.instrumentation.group0instrumentor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.*;

import static org.instrumentation.group0instrumentor.VariableTrackingClassVisitor.localVariableInfo;

/**
 * VariableTrackingClassVisitor
 */
public class VariableTrackingClassVisitor extends ClassVisitor implements LiveVariableTrackingCallBack {

    public static HashMap<String, List<LocalVariable>> localVariableInfo = new HashMap<>();

    public static HashMap<String, List<LocalVariable>> result = new HashMap<>();
//    private String className;

    public VariableTrackingClassVisitor(int api) {
        super(api);
        localVariableInfo = new HashMap<>();
    }
    public VariableTrackingClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
        localVariableInfo = new HashMap<>();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
//        System.err.println("visiting " + name + " " + descriptor);
        MethodVisitor mv = this.cv.visitMethod(access, name, descriptor, signature, exceptions);
        return new LocalVariableVisitor(api, mv, access, name, descriptor, this); // super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public void trackLiveLocalVariablesAtAssert(
            String methodName,
            ArrayList<ArrayList<LocalVariable>> liveVariablesAtAsserts) {
    }
}

interface LiveVariableTrackingCallBack {

    void trackLiveLocalVariablesAtAssert(String methodName, ArrayList<ArrayList<LocalVariable>> liveVariablesAtAsserts);

}

class LocalVariableVisitor extends AdviceAdapter {

    private ArrayList<VisitedInsn> insns = new ArrayList<>();
    private ArrayList<LocalVariable> localVars = new ArrayList<>();
    //    public ArrayList<ExceptionRange> tryCatchRanges = new ArrayList<>();
    private final LiveVariableTrackingCallBack liveVariableTracker;
    private final String methodName;

    public LocalVariableVisitor(int api,
                                final MethodVisitor mv,
                                final int access,
                                final String name,
                                final String descriptor,
                                LiveVariableTrackingCallBack liveVarTracker) {

        super(api, mv, access, name, descriptor);
        this.liveVariableTracker = liveVarTracker;
        this.methodName = name + descriptor + access;
    }

    @Override
    public void visitLabel(Label label) {

        insns.add(VisitedInsn.makeLabel(label));
        super.visitLabel(label);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {

        if (opcode != Opcodes.LSTORE && opcode != Opcodes.DSTORE && opcode != Opcodes.ISTORE && opcode != Opcodes.ASTORE && opcode != Opcodes.FSTORE) {
            super.visitVarInsn(opcode, var);
            return;
        }
        super.visitVarInsn(opcode, var);
        insns.add(VisitedInsn.makeVarInst(opcode,var));
    }

    /**
     * Reason the local variable type based on the store instruction information,
     * take care of aload, lload, dload, fload, iload separate,
     * @param targetInst
     * @return
     */
    public LocalVariable reasonLocalFrom(VisitedVarInst targetInst) {
//        System.err.println("reason for insn " + targetInst.index);
        int targetIndex = targetInst.index;
        List<Label> labels = findStartEndForVarInst(targetInst);
        int startIndex = labels.get(0).getOffset();
        int endIndex = labels.get(1).getOffset();
//        System.err.println("surrounded labels" + startIndex + " " + endIndex);

        LocalVariable resultLoc = null;
        for (LocalVariable lc:this.localVars) {
            if (endIndex >= lc.startOffset() && startIndex < lc.endOffset() && lc.varIndex == targetIndex) {
                resultLoc = lc;
//                System.err.println(lc.startOffset() + " " + lc.endOffset() + lc.desc + " " + lc.name );
                break;
            }
        }
        return resultLoc;
    }

    public List<Label> findStartEndForVarInst(VisitedVarInst targetInst) {

        int varInstIndex = this.insns.indexOf(targetInst);
        int startLabelIndex = varInstIndex - 1;
        while (this.insns.get(startLabelIndex).getType() != VisitedInsn.Type.Label) {
            startLabelIndex--;
        }
        int endLabelIndex = varInstIndex + 1;
        while (this.insns.get(endLabelIndex).getType() != VisitedInsn.Type.Label) {
            endLabelIndex++;
        }
        return Arrays.asList(((VisitedLabel)this.insns.get(startLabelIndex)).label, ((VisitedLabel)this.insns.get(endLabelIndex)).label);

    }

    public List<LocalVariable> computeTypeOfVarInsn() {
//        System.err.println("start computing");
        List<LocalVariable> results = new ArrayList<>();
        for (VisitedInsn insn : this.insns) {
            if (insn.getType() == VisitedInsn.Type.AssertInvoke) {
                VisitedVarInst var = (VisitedVarInst) insn;
                results.add(reasonLocalFrom(var));
            }
        }
        return results;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
//        System.out.println("visiting local variable"+" "+name+start.getOffset()+" "+end.getOffset() + " " + descriptor + " at index " + index);
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
        if (!localVariableInfo.containsKey(this.methodName)) {
            localVariableInfo.put(this.methodName, new ArrayList<>());
        }
        localVariableInfo.get(this.methodName).add(new LocalVariable(name, descriptor, index, start, end));
        localVars.add(new LocalVariable(name, descriptor, index, start, end));
    }

    public void sortLocalBasedOnStartIndex() {
        Collections.sort(this.localVars, new Comparator<LocalVariable>() {
            @Override
            public int compare(LocalVariable v1, LocalVariable v2) {
                return Integer.compare(v1.startOffset(), v2.startOffset());
            }
        });
    }

    @Override
    public void visitEnd() {
//        System.err.println(this.localVars);
        super.visitEnd();
        sortLocalBasedOnStartIndex();
        VariableTrackingClassVisitor.result.put(this.methodName, this.computeTypeOfVarInsn());
    }
}

class LocalVariable {
    public final String name;
    public final String desc;
    public final int varIndex;
    public final Label start;
    public final Label end;

    public LocalVariable(final String name,
                         final String desc,
                         final int varIndex,
                         final Label start,
                         final Label end) {
        this.name = name;
        this.desc = desc;
        this.varIndex = varIndex;
        this.start = start;
        this.end = end;
    }

    public int startOffset() {
        return start.getOffset();
    }

    public int endOffset() {
        return end.getOffset();
    }

    public int loadOpcode() {
        switch (desc) {
            case "I": // int
                return Opcodes.ILOAD;
            case "F": // float
                return Opcodes.FLOAD;
            case "J": // long
                return Opcodes.LLOAD;
            case "D": // double
                return Opcodes.DLOAD;
            default:
                if (desc.length() == 1) {
                    // likely loading a char/byte
                    // default to ILOAD
                    return Opcodes.ILOAD;
                }

                // Array or Object references need ALOAD.
                return Opcodes.ALOAD;
        }
    }

    static String[] names = { "ILOAD", "LLOAD", "FLOAD", "DLOAD", "ALOAD" };

    public String loadOpcodeName() {
        int loadOpcode = loadOpcode();
        return LocalVariable.names[loadOpcode - Opcodes.ILOAD];
    }
}

/**
 * VisitedInsn
 */
abstract class VisitedInsn {
    static enum Type {
        Label, AssertInvoke
    }

    abstract Type getType();

    static VisitedInsn makeLabel(final Label label) {
        return new VisitedLabel(label);
    }

    static VisitedInsn makeAssertInvoke(final int opcode, final String name, final String desc, final String owner) {
        return new VisitedAssertInvoke(opcode, name, desc, owner);
    }

    static VisitedInsn makeVarInst(final int opcode, final int index) {
        return new VisitedVarInst(opcode, index);
    }

}

class VisitedLabel extends VisitedInsn {

    public final Label label;

    public VisitedLabel(final Label label) {
        this.label = label;
    }

    @Override
    Type getType() {
        return Type.Label;
    }

}

class VisitedAssertInvoke extends VisitedInsn {

    public final String name;
    public final String desc;
    public final String owner;

    public VisitedAssertInvoke(final int opcode, final String name, final String desc, final String owner) {
        this.name = name;
        this.desc = desc;
        this.owner = owner;
    }

    @Override
    Type getType() {
        return Type.AssertInvoke;
    }

}

class VisitedVarInst extends VisitedInsn {

    public final int opcode;

    public final int index;

    public VisitedVarInst(final int opcode, final int index) {
        this.opcode = opcode;
        this.index = index;
    }

    @Override
    Type getType() {
        return Type.AssertInvoke;
    }

}



class ExceptionRange{
    Label start;
    Label end;
    Label handler;
    public ExceptionRange(Label start,Label end,Label handler){
        this.start = start;
        this.end = end;
        this.handler = handler;
    }
}
