package org.preruninstrumentation;

import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Type.getMethodDescriptor;

public class GroupZeroUtility {

    public static Map<String, Method> methodDict = new HashMap<>();

    /**
     * Scan the object for its accessors
     * @param o target object
     * @return void
     */
    public static void scanAccessorMethods(Object o) {
        Class<?> objClass = o.getClass();
        // Iterate over all declared methods of the class
        for (Method method : objClass.getMethods()) {
            if (isSimpleGetterMethod(method)) {
                boolean isReturnPrimitiveOrString = GroupZeroUtility.isReturnPrimitiveOrString(method);
                MethodInvocationRecord record = new MethodInvocationRecord(Opcodes.INVOKEVIRTUAL,
                        method.getDeclaringClass().getName().replace(".","/"),
                        method.getName(), getMethodDescriptor(method), false,
                        // the return value of a method invocation won't be an interface variable
                        isReturnPrimitiveOrString);
                methodDict.put(method.getDeclaringClass().getName()+ "." + method.getName() + getMethodDescriptor(method), method);
            }
        }
    }


    /**
     * Prerequisite: o i not null; Scan the object for its accessors methods' states
     * @param o target object
     * @return void
     */
    public static Map<String, String> scanAccessorMethodsToStates(Object o) {
        Map<String, String> states = new HashMap<>();
        Class<?> objClass = o.getClass();
        // Iterate over all declared methods of the class
        for (Method method : objClass.getMethods()) {
            if (isSimpleGetterMethod(method)) {

                boolean isReturnPrimitiveOrString = GroupZeroUtility.isReturnPrimitiveOrString(method);
                MethodInvocationRecord record = new MethodInvocationRecord(Opcodes.INVOKEVIRTUAL,
                        method.getDeclaringClass().getName().replace(".","/"),
                        method.getName(), getMethodDescriptor(method), false,
                        // the return value of a method invocation won't be an interface variable
                        isReturnPrimitiveOrString);
                methodDict.put(method.getDeclaringClass().getName()+ "." + method.getName() + getMethodDescriptor(method), method);
            }
        }
        return states;
    }

    /**
     * Determine if a given method is a getter
     * @param method
     * @return
     */
    // Helper method to determine if a given method is a getter
    public static boolean isSimpleGetterMethod(Method method) {

        // Getter methods must be public or protected
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers)) {
            return false;
        }
        // Getter methods must not take any parameters
        if (method.getParameterTypes().length != 0) return false;
        // Getter methods typically start with "get"
        if (method.getName().startsWith("get") && method.getName().length() > 3) {
            return true;
        }
        // or start with "is" (for booleans)
        if (method.getName().startsWith("is") && method.getName().length() > 2 &&
                method.getReturnType().equals(boolean.class)) {
            return true;
        }
        return false;
    }


    public static boolean isReturnPrimitiveOrString(Method method) {
        return !method.getReturnType().getName().equals("void") &&
                (method.getReturnType().isPrimitive() || method.getReturnType().equals(String.class));
    }
}
