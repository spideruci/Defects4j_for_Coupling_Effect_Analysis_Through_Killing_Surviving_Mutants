package org.preruninstrumentation;

import org.general.BaseExampleClass;
import org.general.ExampleClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.preruninstrumentation.GroupZeroUtility.isReturnPrimitiveOrString;
import static org.preruninstrumentation.GroupZeroUtility.isSimpleGetterMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Type.getMethodDescriptor;

public class TestGroupZeroUtility {


    @Test
    public void testGetOne() {
        BaseExampleClass b = new ExampleClass();
        new ExampleClass().getX();
        assertEquals(11, b.getOne());
    }

    public static Map<String, Method> methodDict = new HashMap<>();

    static {
        BaseExampleClass e = new ExampleClass();
        Class<?> objClass = e.getClass();
        for (Method method : objClass.getMethods()) {
            boolean isReturnPrimitiveOrString = GroupZeroUtility.isReturnPrimitiveOrString(method);
            MethodInvocationRecord record = new MethodInvocationRecord(Opcodes.INVOKEVIRTUAL,
                    method.getDeclaringClass().getName().replace(".","/"),
                    method.getName(), getMethodDescriptor(method), false,
                    // the return value of a method invocation won't be an interface variable
                    isReturnPrimitiveOrString);
            if (isSimpleGetterMethod(method) && !isReturnPrimitiveOrString(method)) {
                try {
                    Method m = objClass.getMethod(method.getName());
                    System.err.println(method.getName() + "-" + (((Object) m.invoke(e)) == null));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                    System.err.println("exception 1");
                    throw new RuntimeException(ex);
                }
            } else {
                if (isSimpleGetterMethod(method) && isReturnPrimitiveOrString(method)) {
                    try {
                        Method m = objClass.getMethod(method.getName());
                        String result = "" + m.invoke(e);

                        System.err.println("let's see for " + m.getName() + result);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                        System.err.println("exception 2");
                        throw new RuntimeException(ex);
                    }

                }
            }
            methodDict.put(method.getDeclaringClass().getName()+ "." + method.getName() + getMethodDescriptor(method), method);
        }
    }

    public static Arguments[] provideMethods() {
        return new Arguments[]{
                Arguments.of("java.lang.Object.notifyAll()V", false, false),
                Arguments.of("java.lang.Object.equals(Ljava/lang/Object;)Z", false,true),
                Arguments.of("java.lang.Object.hashCode()I", false, true),
                Arguments.of("java.lang.Object.wait(JI)V", false, false),
                Arguments.of("org.general.ExampleClass.getName()Ljava/lang/String;", true, true),
                Arguments.of("org.general.ExampleClass.getOne()I", true, true),
                Arguments.of("java.lang.Object.wait(J)V", false, false),
                Arguments.of("java.lang.Object.wait()V", false, false),
                Arguments.of("org.general.ExampleClass.getNull()Ljava/lang/Object;", true, false),
                Arguments.of("org.general.ExampleClass.getNumber()I", true, true),
                Arguments.of("java.lang.Object.toString()Ljava/lang/String;", false, true),
                Arguments.of("java.lang.Object.getClass()Ljava/lang/Class;", true, false),
                Arguments.of("java.lang.Object.notify()V", false, false),
                Arguments.of("org.general.ExampleClass.getExtendedName(Ljava/lang/String;)Ljava/lang/String;", false, true),
                Arguments.of("org.general.ExampleClass.doSomething()V", false, false),
                Arguments.of("org.general.ExampleClass.getNameWithPara(Ljava/lang/String;)Ljava/lang/String;", false, true),
        };
    }

    /**
     * test both the isSimpleGetter and isReturnPrimitiveOrString methods
     * @param descriptor
     * @param expected_isSimpleGetter
     * @param expected_isReturnPrimitiveOrString
     */
    @ParameterizedTest
    @MethodSource("provideMethods")
    public void testIsSimpleGetter(String descriptor, boolean expected_isSimpleGetter, boolean expected_isReturnPrimitiveOrString) {
        Method m = methodDict.get(descriptor);
        assertEquals(expected_isSimpleGetter, isSimpleGetterMethod(m));
        assertEquals(expected_isReturnPrimitiveOrString, isReturnPrimitiveOrString(m));
    }

}


