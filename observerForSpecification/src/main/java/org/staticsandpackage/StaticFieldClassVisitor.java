/**
 * This class tracks the actual access of static fields for the whole program
 */
package org.staticsandpackage;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import static org.staticsandpackage.Utils.isPrimitive;

public class StaticFieldClassVisitor extends ClassVisitor {

    private String className;

    private int access;

    public StaticFieldClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        DomainManagement.domains.add(this.className.replace("/",".").split("\\$")[0]);
        DomainManagement.packages.add(this.className.substring(0,this.className.lastIndexOf("/")).replace("/","."));
        this.access = access;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        boolean isPrimitiveOrString = isPrimitive(descriptor) | descriptor.equals("Ljava/lang/String;");
        boolean isFinal = (access & Opcodes.ACC_FINAL) != 0;
        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        boolean isEnum = (access & Opcodes.ACC_ENUM) != 0;

        // check fields' accessibility:
        // field is not public, class is not public, field name start with "$"
        if (isStatic && (this.access & Opcodes.ACC_PUBLIC) == 1 && isPublic && !name.startsWith("$") && !isEnum) {
            // some final simple-typed fields can be skipped
            boolean shouldSkip = isPrimitiveOrString & isFinal & isStatic;//"Lorg/apache/commons/logging/Log;"
            if (!shouldSkip) {
//                System.err.println("isStatic: " + isStatic + " isFinal: " + isFinal + " isPrimitiveOrString: " + isPrimitiveOrString + " isPublic: " + isPublic + " isEnum: " + isEnum + " name: " + name + " descriptor: " + descriptor + " signature: " + signature + " value: " + value);
//                System.err.println(className + "-" + name + "-" + descriptor);
                StaticInfoRecorder.fields.add(className + "-" + name + "-" + descriptor);
            }
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

}
