package org;

import org.general.ExampleClass;
import org.helper.graph.Arguments;
import org.helper.graph.Pair;
import org.junit.jupiter.api.Test;
import org.testGraph.TestGraph;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.BaseStream;

import static org.helper.graph.Graph.getPrefix;

public class AdhocTest extends TestGraph {

    @Test
    public void testArray() {
        char[] x = new char[2];
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            System.out.println(element);
        }
    }

    @Test
    public void testHaha() {
        ExampleClass obj = new ExampleClass(new ExampleClass());
        LinkedList x = new LinkedList(Arrays.asList(new Object(), new Object()));
        System.err.println(x);
        Class<?> clazz = this.getClass(); // try changing to String.class, ArrayList.class, etc.

        try {
            Method toStringMethod = clazz.getMethod("toString");
            boolean overrides = toStringMethod.getDeclaringClass() != Object.class;

            System.out.println(clazz.getName() +
                    (overrides ? " overrides" : " does NOT override") +
                    " toString()");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


//        String[] x = new String[10];
//        System.err.println(Arrays.toString(x ));
    }

    @Deprecated
    private static final List<String> forbiddenMethods;

    private static final List<String> forbiddenClasses;

    private static final List<String> forbiddenPackages;

    static {
        forbiddenMethods = new ArrayList<>();
        forbiddenMethods.add("equals");
        forbiddenMethods.add("notify");
        forbiddenMethods.add("notifyAll");
        forbiddenMethods.add("wait");
        forbiddenMethods.add("getClass");
        forbiddenMethods.add("display");
        forbiddenMethods.add("clone");
        forbiddenMethods.add("hasExtensions");
        forbiddenMethods.add("hashCode");
        forbiddenMethods.add("toString");

        // since we generate contains(), we don't need to observe iterators
        forbiddenMethods.add("iterator");
        forbiddenMethods.add("spliterator");
        forbiddenMethods.add("listIterator");
        forbiddenMethods.add("stream");
        forbiddenMethods.add("parallelStream");
        forbiddenMethods.add("reverse");
        forbiddenMethods.add("clear");

        forbiddenClasses = new ArrayList<>();
        forbiddenClasses.add("java.lang.Object");
        forbiddenClasses.add("java.lang.Class");
        forbiddenClasses.add("java.lang.Enum");
        forbiddenClasses.add("java.util.Date");
        forbiddenClasses.add("java.net.URL");
        forbiddenClasses.add("java.util.Calendar");
        forbiddenClasses.add("java.io.File");

        forbiddenPackages = new ArrayList<>();
        forbiddenPackages.add("java.time");
    }



    public static String getterState(Object obj, int depth, String result) {
        if (depth > Arguments.depth) {
            return result;
        }

        // Get the Class object of the instance
        Class<?> clazz = obj.getClass();

        // Get all methods of the class
        Method[] methods = clazz.getMethods();

        // Iterate through the methods and invoke getters
        for (Method method : methods) {

            if (isValidMethod(method) && isGetter(method.getName()) && method.getParameterCount() == 0 && Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(clazz.getModifiers())) {
                // Invoke the getter method

                try {
                    Object value;
                    value = method.invoke(obj);
                    if (isComplexObjectType(value)) {
                        result += "\n" + getPrefix(depth) + method.getName() + " = complex";
                        result = result +  "\n" + getterState(value, depth + 1, "");
                    } else {
                        result += "\n" + getPrefix(depth) + method.getName() + " = " + getText(value);
                    }
                } catch (Exception e) {
                    // report
                }

            }
        }
        if (result.startsWith("\n")) {
            result = result.substring(1);
        }
        return result;
    }

    private static boolean isVoid(Class<?> type) {
        return type.equals(Void.class) || type.equals(void.class);
    }


    private static boolean returnStream(Method method) {
        try {
            return BaseStream.class.isAssignableFrom(method.getReturnType());
        } catch (Exception e) {
            return false;
        }
    }

    private static String getText (Object value) {
        if (value instanceof String[]) {
            return Arrays.toString((String[]) value);
        } else if (value instanceof int[]) {
            return Arrays.toString((int[]) value);
        } else if (value instanceof long[]) {
            return Arrays.toString((long[]) value);
        } else if (value instanceof double[]) {
            return Arrays.toString((double[]) value);
        } else if (value instanceof float[]) {
            return Arrays.toString((float[]) value);
        } else if (value instanceof short[]) {
            return Arrays.toString((short[]) value);
        } else if (value instanceof byte[]) {
            return Arrays.toString((byte[]) value);
        } else if (value instanceof char[]) {
            return Arrays.toString((char[]) value);
        } else if (value instanceof boolean[]) {
            return Arrays.toString((boolean[]) value);
        } else if (value instanceof Integer[]) {
            return Arrays.toString((Integer[]) value);
        } else if (value instanceof Long[]) {
            return Arrays.toString((Long[]) value);
        } else if (value instanceof Double[]) {
            return Arrays.toString((Double[]) value);
        } else if (value instanceof Float[]) {
            return Arrays.toString((Float[]) value);
        } else if (value instanceof Short[]) {
            return Arrays.toString((Short[]) value);
        } else if (value instanceof Byte[]) {
            return Arrays.toString((Byte[]) value);
        } else if (value instanceof Character[]) {
            return Arrays.toString((Character[]) value);
        } else if (value instanceof Boolean[]) {
            return Arrays.toString((Boolean[]) value);
        } else {
            return "" + value;
        }
    }

    public static boolean isValidMethod(Method method) {
        if (!Modifier.isPublic(method.getModifiers())  // the method is not public
                || method.getParameterCount() > 0 // there is parameters
                || Modifier.isStatic(method.getModifiers()) // the method is static
                || isVoid(method.getReturnType()) // the method is return void type, i.e. it returns nothing
                || method.getReturnType() == Class.class // the method returns Class<?>
                || !Modifier.isPublic(method.getReturnType().getModifiers())  // the method return a type that is not visible, i.e. is not public.
                || forbiddenClasses.contains(method.getReturnType().getName()) // it returns a forbidden type
                || returnStream(method) // the method return a stream
                || method.getParameterTypes().length > 0 // the method hasParameter
                || isDefaulttoStringOrHashCode(method) // we don't use default implementation of toString() and hashCode, i.e. implementation of Object and Enum
        ) {
            return false;
        }

        // we rely on convention name: get, is, should
        // TODO expand the scope of assertion to other pure method, with return type etc...

        return isGetter(method.getName());
    }

    @Deprecated
    static // since we forbid the class Object
    boolean isDefaulttoStringOrHashCode(Method method) {
        if (method.getDeclaringClass() == null) {
            return false;
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        if ("hashCode".equals(method.getName())) {
            return isDefaultClass(declaringClass.getName()) ||
                    declaringClass.getName().equals("java.net.URL") ||
                    declaringClass.getName().equals("java.net.URI");
        } else {
            return "toString".equals(method.getName()) &&
                    isDefaultClass(declaringClass.getName());
        }
    }



    @Deprecated
    private static boolean isDefaultClass(String qualifiedName) {
        return ("java.lang.Enum".equals(qualifiedName) || "java.lang.Object".equals(qualifiedName));
    }

    public static boolean isGetter(String name) {
        return name.startsWith("has") ||
                name.startsWith("get") ||
                name.startsWith("is") ||
                name.startsWith("should") ||
                name.equals("toString") ||
                name.startsWith("hashCode");
    }


    public static boolean isBoxedPrimitiveOrString(Object o) {
        return o instanceof Integer || o instanceof Long || o instanceof Double || o instanceof Float || o instanceof Short || o instanceof Byte || o instanceof Character || o instanceof Boolean || o instanceof String;
    }

    public static boolean isBoxedPrimitiveArray(Object o) {
        return o instanceof Integer[] || o instanceof Long[] || o instanceof Double[] || o instanceof Float[] || o instanceof Short[] || o instanceof Byte[] || o instanceof Character[] || o instanceof Boolean[];
    }

    public static boolean isStringArray(Object o) {
        return o instanceof String[];
    }

    public static boolean isPrimitiveArray(Object o) {
        return o instanceof int[] || o instanceof long[] || o instanceof double[] || o instanceof float[] || o instanceof short[] || o instanceof byte[] || o instanceof char[] || o instanceof boolean[];
    }

    public static boolean isComplexObjectType(Object o) {
        if (o == null || isBoxedPrimitiveOrString(o) || isBoxedPrimitiveArray(o) || isPrimitiveArray(o) || isStringArray(o)) {
            return false;
        }
        return true;
    }
}
