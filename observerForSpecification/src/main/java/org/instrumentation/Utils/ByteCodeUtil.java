package org.instrumentation.Utils;

import org.objectweb.asm.Type;

public class ByteCodeUtil {

    public static String getReturnType(String methodDesc) {
        return Type.getReturnType(methodDesc).toString();
    }
}
