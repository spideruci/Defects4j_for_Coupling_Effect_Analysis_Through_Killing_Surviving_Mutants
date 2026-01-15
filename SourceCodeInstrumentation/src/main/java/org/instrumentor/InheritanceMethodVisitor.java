package org.instrumentor;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM9;

public class InheritanceMethodVisitor extends MethodVisitor {

    public InheritanceMethodVisitor(MethodVisitor mv) {
        super(ASM9, mv);
    }

    // You can add method-level instrumentation here if needed.
    @Override
    public void visitCode() {
        super.visitCode();
    }
}
