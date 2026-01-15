package org.instrumentation.group0instrumentor;

import org.instrumentforsubject.DoInstrumentation;
import org.instrumentforsubject.Scope;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.staticsandpackage.DomainManagement;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.instrumentation.Utils.ByteCodeUtil.getReturnType;
import static org.objectweb.asm.Opcodes.*;

public class StateMonitorMethodVisitor extends MethodVisitor {

    private int lineNumber;

    // A shared counter map: key = return site signature, value = current index
    private static final Map<String, Integer> returnSiteCounters = new HashMap<>();

    private static int nextOrdinal(String siteKey) {
        int current = returnSiteCounters.getOrDefault(siteKey, 0);
        returnSiteCounters.put(siteKey, current + 1);
        return current;
    }

    private static final Map<String, Integer> newArrayCounters = new HashMap<>();

    private static int nextNewArrayOrdinal(String siteKey) {
        int current = newArrayCounters.getOrDefault(siteKey, 0);
        newArrayCounters.put(siteKey, current + 1);
        return current;
    }

    private static final Map<String, Integer> arrayCounters = new HashMap<>();

    private static int nextArrayOrdinal(String siteKey) {
        int current = arrayCounters.getOrDefault(siteKey, 0);
        arrayCounters.put(siteKey, current + 1);
        return current;
    }



    private static final Map<String, Integer> multiArrayCounters = new HashMap<>();

    private static int nextMultiArrayOrdinal(String siteKey) {
        int current = multiArrayCounters.getOrDefault(siteKey, 0);
        multiArrayCounters.put(siteKey, current + 1);
        return current;
    }



    private static final Map<String, Integer> localStoreCounters = new HashMap<>();

    private static int nextLocalOrdinal(String siteKey) {
        int current = localStoreCounters.getOrDefault(siteKey, 0);
        localStoreCounters.put(siteKey, current + 1);
        return current;
    }

    private static final Map<String, Integer> fieldGetCounters = new HashMap<>();

    private static int nextFieldOrdinal(String siteKey) {
        int current = fieldGetCounters.getOrDefault(siteKey, 0);
        fieldGetCounters.put(siteKey, current + 1);
        return current;
    }




    private String enclosingMethod;

    private String fileLoc;

    private int firstFreeLocal;

    private int excLocal;

    private String classInternalName;

    private String testClassName;

    List<LocalVariable> localVariableList;

    private final Label startL = new Label();

    private final Label endL   = new Label();

    private final Label handlerL = new Label();

    private boolean hasTestAnnotation = false;

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals("Lorg/junit/Test;") || descriptor.equals("Lorg/junit/jupiter/api/Test;")) {
//            System.err.println("This is a test annotation");
            hasTestAnnotation = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    public StateMonitorMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String desc, String fileLoc, String className, String classInternalName) {
        super(api, methodVisitor);
        this.enclosingMethod = name +  "-" + desc;
        this.fileLoc = fileLoc;
        this.testClassName = className;
        this.classInternalName = classInternalName;
        this.localVariableList = VariableTrackingClassVisitor.result.get(name + desc + access);

        // compute first free local (works with COMPUTE_MAXS/FRAMES)
        int idx = ((access & ACC_STATIC) != 0) ? 0 : 1;
        for (Type t : Type.getArgumentTypes(desc)) idx += t.getSize();
        this.firstFreeLocal = idx;
        this.excLocal = idx; // will use this slot for the exception
    }

    @Override
    public void visitCode() {
//        System.err.println("visit code executed");
        super.visitCode();
        //TODO now only work for junit 3

        if (isJUnitMethod(enclosingMethod)) {
            mv.visitMethodInsn(INVOKESTATIC, "org/helper/TesExtension", "onTestMethodStart", "()V", false);
            beginTry();
        }
        // for constructors we wait until after super(..) invokespecial
    }

    private void beginTry() {
        // Mark start of protected region
        super.visitLabel(startL);
    }


    // If the method is a JUnit 3 test method, we need to instrument it.
    public boolean isJUnitMethod(String enclosingMethod) {
//        return true;

        // JUnit 3
        if (enclosingMethod.startsWith("test") && InheritanceRelationships.extendsJUnit3(classInternalName)) {
            return true;
        }
        // JUnit 4 or JUnit 5
        if (hasTestAnnotation) {
            return true;
        }
//        return true; // temporarily for debugging
        return false;
    }



    // On each normal return, call finally-logic first.
    @Override
    public void visitInsn(int opcode) {

        if (!isJUnitMethod(enclosingMethod)) {
            super.visitInsn(opcode);
            return;
        }

        switch (opcode) {
            case RETURN:
            case IRETURN:
            case FRETURN:
            case ARETURN:
            case LRETURN:
            case DRETURN:
                emitFinally();
                super.visitInsn(opcode);
                return;
            default:
                super.visitInsn(opcode);
        }

    }

    private void emitFinally() {
        mv.visitMethodInsn(INVOKESTATIC, "org/helper/TesExtension", "onTestMethodEnd", "()V", false);
    }

    // Close the try region and lay down the handler block that rethrows.
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {

        if (isJUnitMethod(enclosingMethod)) {
            mv.visitLabel(endL);
            // Register try/catch (Throwable) covering [startL, endL)
//            mv.visitTryCatchBlock(startL, endL, handlerL, "java/lang/Throwable");

            // Handler: rethrow immediately (no instrumentation/side effects)
            mv.visitLabel(handlerL);
            mv.visitVarInsn(ASTORE, excLocal); // catch param
            emitFinally();
            mv.visitVarInsn(ALOAD, excLocal);
            mv.visitInsn(ATHROW);

            super.visitTryCatchBlock(startL, endL, handlerL, "java/lang/Throwable");
            super.visitMaxs(maxStack, Math.max(maxLocals, excLocal + 1));
        }
        else {
            super.visitMaxs(maxStack, maxLocals);
        }




    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
//        System.err.println("visitMethodInsn executed");
        if (!isJUnitMethod(enclosingMethod)) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        }

        Label L = new Label();
        super.visitLabel(L);
        if (lineNumber >= 0) super.visitLineNumber(lineNumber, L);

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        String returnType = getReturnType(descriptor);

        if ((returnType.equals("V") && !name.equals("<init>")) || name.equals("<clinit>")) {
            return;
        }
        if (returnType.equals("V") && name.equals("<init>") && enclosingMethod.startsWith("<init>")) {
            return;
        }

        String filter = "";
        if (DoInstrumentation.scope == Scope.ALL) {
            filter = "";
        } else if (DoInstrumentation.scope == Scope.PROJECT) {
            filter = DomainManagement.projectDomain;
        } else if (DoInstrumentation.scope == Scope.PACKAGE) {
            filter = DomainManagement.getPackageDomain(testClassName);
        } else if (DoInstrumentation.scope == Scope.CLASS) {
            filter = DomainManagement.getClassDomain(testClassName);
        }

        boolean shouldInstrument = owner.replace("/", ".").startsWith(filter)
                | returnType.substring(1).replace("/", ".").startsWith(filter);

        if (!shouldInstrument) {
            return;
        }


        // update the line number


        // unique site key
        String siteKey = fileLoc + "-" + lineNumber + "-" + owner + "-" + name + "-" + descriptor;
        int ordinal = nextOrdinal(siteKey);



        switch (returnType) {
            case "I":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processInteger", "(ILjava/lang/String;Ljava/lang/String;I)I", false);
                break;
            case "J":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processLong", "(JLjava/lang/String;Ljava/lang/String;I)J", false);
                break;
            case "D":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processDouble", "(DLjava/lang/String;Ljava/lang/String;I)D", false);
                break;
            case "F":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processFloat", "(FLjava/lang/String;Ljava/lang/String;I)F", false);
                break;
            case "B":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processByte", "(BLjava/lang/String;Ljava/lang/String;I)B", false);
                break;
            case "C":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processChar", "(CLjava/lang/String;Ljava/lang/String;I)C", false);
                break;
            case "S":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processShort", "(SLjava/lang/String;Ljava/lang/String;I)S", false);
                break;
            case "Z":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processBoolean", "(ZLjava/lang/String;Ljava/lang/String;I)Z", false);
                break;
            case "Ljava/lang/String;":
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal); // new ordinal argument
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processString", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;", false);
                break;
            default:
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("return-" + owner + "-" + name + "-" + descriptor);
                mv.visitLdcInsn(ordinal);
                mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                        "processObject", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V", false);
                break;
        }
    }

    /**
     * This approach to get the line numbers has some limitations:
     * The JVM bytecode may not map perfectly 1-to-1 with Java source lines:
     * Compiler may fold multiple statements into one bytecode line.
     * One source line can generate multiple bytecode ranges (loops, ternaries, lambdas).
     * Sometimes compilers inject synthetic lines (like line 0).
     * If the class was compiled without -g:lines, line number info may be missing or incomplete.
     * For inlined/multi-line expressions (like foo = new Foo().foo()), the line table usually only points to the start, not each continuation line.
     * @param line
     * @param start
     */
    @Override
    public void visitLineNumber(int line, Label start) {
        this.lineNumber = line;
        super.visitLineNumber(line, start);
    }


    @Override
    public void visitVarInsn(int opcode, int var) {
        if (!isJUnitMethod(enclosingMethod)) {
            super.visitVarInsn(opcode, var);
            return;
        }
        if (opcode != Opcodes.LSTORE && opcode != Opcodes.DSTORE &&
                opcode != Opcodes.ISTORE && opcode != Opcodes.ASTORE &&
                opcode != Opcodes.FSTORE) {
            super.visitVarInsn(opcode, var);
            return;
        }

        LocalVariable loc = localVariableList.isEmpty() ? null : localVariableList.remove(0);
        if (loc == null) {
            super.visitVarInsn(opcode, var);
            return;
        }

        // unique site key for this variable store
        String siteKey = fileLoc + "-" + lineNumber + "-" + var + "-" + loc.desc + "-" + loc.name;
        int ordinal = nextLocalOrdinal(siteKey);

        switch (opcode) {
            case Opcodes.LSTORE:
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("local-" + loc.desc + "-" + loc.name);
                mv.visitLdcInsn(ordinal);
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/helper/InstrumentationUtils",
                        "processLong",
                        "(JLjava/lang/String;Ljava/lang/String;I)J",
                        false);
                break;
            case Opcodes.DSTORE:
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("local-" + loc.desc + "-" + loc.name);
                mv.visitLdcInsn(ordinal);
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/helper/InstrumentationUtils",
                        "processDouble",
                        "(DLjava/lang/String;Ljava/lang/String;I)D",
                        false);
                break;
            case Opcodes.ISTORE:
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("local-" + loc.desc + "-" + loc.name);
                mv.visitLdcInsn(ordinal);
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/helper/InstrumentationUtils",
                        "processInteger",
                        "(ILjava/lang/String;Ljava/lang/String;I)I",
                        false);
                break;
            case Opcodes.ASTORE:
                if ("Ljava/lang/String;".equals(loc.desc)) {
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("local-" + loc.desc + "-" + loc.name);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC,
                            "org/helper/InstrumentationUtils",
                            "processString",
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;",
                            false);
                } else {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("local-" + loc.desc + "-" + loc.name);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC,
                            "org/helper/InstrumentationUtils",
                            "processObject",
                            "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V",
                            false);
                }
                break;
            case Opcodes.FSTORE:
                mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                mv.visitLdcInsn("local-" + loc.desc + "-" + loc.name);
                mv.visitLdcInsn(ordinal);
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/helper/InstrumentationUtils",
                        "processFloat",
                        "(FLjava/lang/String;Ljava/lang/String;I)F",
                        false);
                break;
        }
        super.visitVarInsn(opcode, var);
    }


    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (!isJUnitMethod(enclosingMethod)) {
            super.visitTypeInsn(opcode, type);
            return;
        }

        if (opcode == Opcodes.ANEWARRAY) {
            super.visitTypeInsn(opcode, type);

            // Build a site key and ordinal if you want uniqueness
            String siteKey = fileLoc + "-" + lineNumber + "-" + type;
            int ordinal = nextArrayOrdinal(siteKey);

            mv.visitInsn(Opcodes.DUP); // keep the array reference
            mv.visitLdcInsn(fileLoc + "-" + lineNumber);
            mv.visitLdcInsn("array-" + type);
            mv.visitLdcInsn(ordinal);
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/helper/InstrumentationUtils",
                    "processObject",
                    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V",
                    false);
        } else {
            super.visitTypeInsn(opcode, type);
        }
    }


    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (!isJUnitMethod(enclosingMethod)) {
            super.visitIntInsn(opcode, operand);
            return;
        }
        if (opcode == Opcodes.NEWARRAY) {

            String array_type;
            switch (operand) {
                case Opcodes.T_BOOLEAN: array_type = "boolean"; break;
                case Opcodes.T_CHAR:    array_type = "char";    break;
                case Opcodes.T_FLOAT:   array_type = "float";   break;
                case Opcodes.T_DOUBLE:  array_type = "double";  break;
                case Opcodes.T_BYTE:    array_type = "byte";    break;
                case Opcodes.T_SHORT:   array_type = "short";   break;
                case Opcodes.T_INT:     array_type = "int";     break;
                case Opcodes.T_LONG:    array_type = "long";    break;
                default:                array_type = "unknown"; break;
            }

            super.visitIntInsn(opcode, operand);

            // build a site key and ordinal
            String siteKey = fileLoc + "-" + lineNumber + "-" + array_type;
            int ordinal = nextNewArrayOrdinal(siteKey);

            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(fileLoc + "-" + lineNumber);
            mv.visitLdcInsn("array-" + array_type);
            mv.visitLdcInsn(ordinal);
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/helper/InstrumentationUtils",
                    "processObject",
                    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V",
                    false);

        } else {
            super.visitIntInsn(opcode, operand);
        }
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
        if (!isJUnitMethod(enclosingMethod)) {
            return;
        }

        // Build a unique site key
        String siteKey = fileLoc + "-" + lineNumber + "-" + descriptor + "-" + numDimensions;
        int ordinal = nextMultiArrayOrdinal(siteKey);

        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(fileLoc + "-" + lineNumber);
        mv.visitLdcInsn("array-" + descriptor + "-" + numDimensions);
        mv.visitLdcInsn(ordinal);
        mv.visitMethodInsn(
                INVOKESTATIC,
                "org/helper/InstrumentationUtils",
                "processObject",
                "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V",
                false
        );
    }



    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (!isJUnitMethod(enclosingMethod)) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
            return;
        }
        if ((opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) && !owner.startsWith("org.assertj")) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
            DoInstrumentation.numGetField += 1;

            // build a site key to count occurrences
            String siteKey = fileLoc + "-" + lineNumber + "-" + owner + "-" + name + "-" + descriptor;
            int ordinal = nextFieldOrdinal(siteKey);

            switch (descriptor) {
                case "I":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processInteger", "(ILjava/lang/String;Ljava/lang/String;I)I", false);
                    break;
                case "J":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processLong", "(JLjava/lang/String;Ljava/lang/String;I)J", false);
                    break;
                case "D":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processDouble", "(DLjava/lang/String;Ljava/lang/String;I)D", false);
                    break;
                case "F":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processFloat", "(FLjava/lang/String;Ljava/lang/String;I)F", false);
                    break;
                case "B":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processByte", "(BLjava/lang/String;Ljava/lang/String;I)B", false);
                    break;
                case "C":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processChar", "(CLjava/lang/String;Ljava/lang/String;I)C", false);
                    break;
                case "S":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processShort", "(SLjava/lang/String;Ljava/lang/String;I)S", false);
                    break;
                case "Z":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processBoolean", "(ZLjava/lang/String;Ljava/lang/String;I)Z", false);
                    break;
                case "Ljava/lang/String;":
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processString", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;", false);
                    break;
                default:
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitLdcInsn(fileLoc + "-" + lineNumber);
                    mv.visitLdcInsn("getField-" + owner + "-" + name + "-" + descriptor);
                    mv.visitLdcInsn(ordinal);
                    mv.visitMethodInsn(INVOKESTATIC, "org/helper/InstrumentationUtils",
                            "processObject", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V", false);
                    break;
            }

        } else {
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }



}
