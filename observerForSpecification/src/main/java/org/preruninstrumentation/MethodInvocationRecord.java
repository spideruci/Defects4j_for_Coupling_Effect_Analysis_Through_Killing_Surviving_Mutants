package org.preruninstrumentation;

public class MethodInvocationRecord {
    private int opcode;
    private String owner;
    private String name;
    private String descriptor;
    private boolean isInterface;
    private boolean isReturnPrimitiveOrString;

    public MethodInvocationRecord(int opcode, String owner, String name, String descriptor, boolean isInterface, boolean isReturnPrimitiveOrString) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isInterface = isInterface;
        this.isReturnPrimitiveOrString = isReturnPrimitiveOrString;
    }

    public int getOpcode() {
        return opcode;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean getIsInterface() {
        return isInterface;
    }

    public boolean getIsReturnPrimitiveOrString() {
        return isReturnPrimitiveOrString;
    }

    @Override
    public String toString() {
        return "MethodInvocationRecord{" +
                "opcode=" + opcode +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", isInterface=" + isInterface +
                ", isReturnPrimitiveOrString=" + isReturnPrimitiveOrString +
                '}';
    }
}
