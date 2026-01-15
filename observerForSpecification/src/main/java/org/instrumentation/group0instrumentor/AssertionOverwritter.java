package org.instrumentation.group0instrumentor;


import org.objectweb.asm.*;


public class AssertionOverwritter extends ClassVisitor {

    public AssertionOverwritter(int api, ClassVisitor cv) {
        super(api, cv);
    }

    private String className;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // Add an annotation to the class
        this.className = name;
        // Continue visiting the class as usual
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return new TryCatchAdapter(api, mv, this.className, name);
    }


    static class TryCatchAdapter extends MethodVisitor {

        private int curLine = 0;


        private String className;

        private String methodName;

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            super.visitLineNumber(line, start);
            this.curLine = line;
        }

        public TryCatchAdapter(int api, MethodVisitor mv, String className, String methodName) {
            super(api, mv);
            this.className = className;
            this.methodName = methodName;
        }

        public String getFileName(String s) {
            String temp = s.split("$")[0];
            String[] results = temp.split("/");
            return results[results.length - 1] + ".java";
        }

        public static String insertBeforeLastOccurrence(String input, String target, String insertion) {
            int lastIndex = input.lastIndexOf(target);
            if (lastIndex == -1) {
                return input;  // target not found in input
            }

            StringBuilder sb = new StringBuilder(input);
            sb.insert(lastIndex, insertion);
            return sb.toString();
        }
    }


}