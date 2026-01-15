package org.instrumentation.group0instrumentor;

import org.objectweb.asm.*;

public class StateMonitorClassVisitor extends ClassVisitor{


    private String fileLoc;

    private String className;

    private String classInternalName;

    public StateMonitorClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.fileLoc = name;
        this.classInternalName = name;
        this.className = name.replace("/", ".");
        this.className = this.className.split("\\$")[0];
        super.visit(version, access, name, signature, superName, interfaces);
    }





    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if ((name.startsWith("test") && InheritanceRelationships.extendsJUnit3(classInternalName))) {
            return new StateMonitorMethodVisitor(Opcodes.ASM9, mv, access, name, descriptor, fileLoc, className, classInternalName);
        }
        return new StateMonitorMethodVisitor(Opcodes.ASM9, mv, access, name, descriptor, fileLoc, className, classInternalName);

    }
}
