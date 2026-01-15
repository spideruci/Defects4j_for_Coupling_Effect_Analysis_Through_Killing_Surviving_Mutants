package org.helper;

import org.helper.graph.*;
import org.helper.states.Loc;
import org.helper.states.StateItem;
import org.helper.states.TestState;
import org.helper.states.type;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.BaseStream;

import static org.helper.Utils.addErrorMessage;
import static org.helper.Utils.getStackTraceInfo;
import static org.helper.graph.Arguments.isInitialScan;
import static org.helper.graph.Arguments.stateNumMap;
import static org.helper.graph.Graph.*;

/**
 * This helper class/package is ideally instrumented into the subject project to record the states of the test execution
 */
public class TestExtension implements BeforeEachCallback, AfterEachCallback {

    public static TestState states = new TestState();

    public static Set<Integer> dumpingTiming = new HashSet<>();

    public static String dir;

    public static String f;

    public static boolean inRecording = false;

    public static Object thisClass;

    public static String testID = "nothing";

    private static long t0;

    private static Set<String> testClassName = new HashSet<>();

    private static final List<String> forbiddenMethods;

    private static final List<String> forbiddenClasses;

    private static final List<String> forbiddenPackages;

    private static final List<Object> visitedObjectsForGetters = new ArrayList<>();

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


    /**
     * stop recording states related to the test execution
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent() && !isInitialScan) {
//            String dir = extensionContext.getTestClass().get().getName();
//            String f = extensionContext.getTestMethod().get().getName() + "_" + extensionContext.getUniqueId().hashCode();
//            dumpStates(dir, f, Arguments.depth);
        } else if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent() && isInitialScan) {
            writeInitializeInfo(extensionContext);
        }
        states.clear();
        dir = "";
        f = "";
        dumpingTiming.clear();
        inRecording = false;
//        long t1 = System.nanoTime() - t0;
//        writeTimeInfo(extensionContext.getUniqueId() + "---" + t1);
        System.err.println(extensionContext.getUniqueId() + " after each");
    }

    /**
     * start recording states related to the test execution
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
//        t0 = System.nanoTime();
        states.clear();
        inRecording = true;
        testID = extensionContext.getUniqueId();
        extensionContext.getTestInstance().ifPresent(o -> thisClass = o);

        if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent()) {
            dir = extensionContext.getTestClass().get().getName();
            f = extensionContext.getTestMethod().get().getName() + "_" + extensionContext.getUniqueId().hashCode();
            if (stateNumMap.containsKey(extensionContext.getUniqueId()) && !isInitialScan && Arguments.multiDump) {
                int totalNum = stateNumMap.get(extensionContext.getUniqueId());
                dumpingTiming = splitItems(totalNum, Arguments.numDumps);
            }
        }
        System.err.println(extensionContext.getUniqueId() + " before each");
    }

    public synchronized static void dumpStates(String dir, String fileName, int depth, int repeatIndex) {
        generateDirs("stateData/" + dir);
        writeGroup0InfoToFile("stateData/" + dir + "/" + fileName + "-" + repeatIndex, depth);
    }

    /**
     * generate directories (including nested) based on a provided path
     * @param dir
     */
    public synchronized static void generateDirs(String dir) {
        Path path = Paths.get(dir);
        try {
            // Create directories
            Files.createDirectories(path);
        } catch (Exception e) {
            addErrorMessage(getStackTraceInfo(e));
            System.err.println("Error occurred while creating directories: " + e.getMessage());
        }
    }

    /**
     * write the states of the test execution to a file
     * @param path
     */
    public static void writeGroup0InfoToFile(String path, int depth) {
        List<String> output = new LinkedList<String>();
        StateItem lastState = TestState.states.get(TestState.states.size()-1);
        output.add("States: " + TestState.states.size() + "---" + lastState.getLine() + "-" + lastState.getSource());
        for (int i = 0; i < TestState.states.size(); i++) {
            StateItem s = TestState.states.get(i);
            output.add("*Meta " + s.getLine() + ": " + s.getSource() + " -> " + maskNewLine(s.getContent(),1));
            if (s.getObject() != null) {
                Graph g = new Graph(s.getObject(), depth);
                output.add(g.getString(g.getRoot(), Arguments.depth).substring(1));
            }
        }

        // add this if necessary
        if (Arguments.dumpThis) {
            StateItem thisClass = new StateItem(new Loc("this", "this"), type.Object, "", TestExtension.thisClass);
            output.add("*Meta " + thisClass.getLine() + ": " + thisClass.getSource() + " -> " + maskNewLine(thisClass.getContent(),1));
            if (thisClass.getObject() != null) {
                Graph g = new Graph(thisClass.getObject(), depth);
                output.add(g.getString(g.getRoot(), Arguments.depth).substring(1));
            }
        }


        // add static fields if necessary
        if (Arguments.dumpStatic) {
            for (String content: TestState.staticFields) {
                String[] content_splits = content.split("-");
                String className = content_splits[0];
                String fieldName = content_splits[1];

                try {
                    Class<?> clazz = Class.forName(className);
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value;
                    try {
                        value = field.get(null);
                    } catch (NullPointerException n) {
                        value = null;
                    }

                    if (value != null) {
                        output.add("*Meta " + "static: " + content + " -> " + "non-null");
                        Graph g = new Graph(value, depth);
                        output.add(g.getString(g.getRoot(), Arguments.depth).substring(1));
                    } else {
                        output.add("*Meta " + "static: " + content + " -> " + "null");
                    }

                } catch (ClassNotFoundException e) {
                    addErrorMessage(getStackTraceInfo(e));
                    System.err.println("ClassNotFound Error occurred while getting the class to dump static fields: " + e.getMessage());
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    addErrorMessage(getStackTraceInfo(e));
                    System.err.println("NoSuchField Error occurred while getting the class to dump static fields: " + e.getMessage());
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    addErrorMessage(getStackTraceInfo(e));
                    System.err.println("IllegalArgument Error occurred while getting the class to dump static fields: " + e.getMessage());
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    addErrorMessage(getStackTraceInfo(e));
                    System.err.println("IllegalAccess Error occurred while getting the class to dump static fields: " + e.getMessage());
                    throw new RuntimeException(e);
                }

            }
        }

        StringBuilder equalityOutput = new StringBuilder("Equality:");
        StringBuilder sameOutput = new StringBuilder("Same:");
        for (int i = 0; i < TestState.states.size(); i++) {
            for (int j = i + 1; j < TestState.states.size(); j++) {
                Object o1 = TestState.states.get(i).getObject();
                Object o2 = TestState.states.get(j).getObject();
                if (i != j && TestState.states.get(i).getObject() != null && TestState.states.get(j).getObject() != null && !isBoxedPrimitiveOrString(o1) && !isBoxedPrimitiveOrString(o2)) {
                    try {
                        if (o1.equals(o2)) {
                            equalityOutput.append(" " + i + "," + j);
                        }
                    } catch (Exception e) {
                        // equals method is override and is not supported by users (explicitly forbidden usage)
                    }
                    if (o1 == o2) {
                        sameOutput.append(" " + i + "," + j);
                    }
                }
            }
        }
        output.add(equalityOutput.toString());
        output.add(sameOutput.toString());

        for (int i = 0; i < TestState.states.size(); i++) {
            StateItem s = TestState.states.get(i);
            output.add("*GetterMeta " + s.getLine() + ": " + s.getSource() + " -> " + maskNewLine(s.getContent(),1));
            if (s.getObject() != null) {
                visitedObjectsForGetters.clear();
                String tempResult = getterState(s.getObject(), 1, "");
                visitedObjectsForGetters.clear();
                if (tempResult.equals("\n") || tempResult.equals("")) {
                } else {
                    output.add(tempResult);
                }
            }
        }

        if (Arguments.dumpThis) {
            StateItem thisClass = new StateItem(new Loc("this", "this"), type.Object, "", TestExtension.thisClass);
            output.add("*Meta " + thisClass.getLine() + ": " + thisClass.getSource() + " -> " + maskNewLine(thisClass.getContent(),1));
            if (thisClass.getObject() != null) {
                Object obj = thisClass.getObject();
                Class<?> clazz = thisClass.getObject().getClass();
                List<Field> allFields = new LinkedList<>();
                while (clazz != null && clazz != Object.class) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        // non-hidden fields, non-transient fields
                        if (!field.getName().startsWith("$") && !Modifier.isTransient(field.getModifiers()) && !isUnMutable(field))  {
                            allFields.add(field);
                        }
                    }
                    clazz = clazz.getSuperclass();  // Move to the superclass
                }

                Iterator<Field> iterator = allFields.iterator();
                while (iterator.hasNext()) {
                    Field field = iterator.next();
                    try {
                        field.setAccessible(true);
                        try {
                            // Get the value of each field
                            Object value = field.get(obj);
                            if (isSimpleObject(value)) {
                                output.add("*GetterMetaThis " + field.getName() + " = " + maskNewLine(getText(value), 1));
                            } else {
                                visitedObjectsForGetters.clear();
                                String tempResult = getterState(value, 1, "");
                                visitedObjectsForGetters.clear();
                                if (tempResult.equals("\n") || tempResult.equals("")) {
                                } else {
                                    output.add("*GetterMetaThis " + field.getName() + " = " + "complex");
                                    output.add(tempResult);
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        throw e;
                        // ignore, usually inaccessible
                    }
                }

            }
        }


        // recursively use getters

//        output = getSimplifiedList(output);
        writeFile(path, output);
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

    public static String getterState(Object obj, int depth, String result) {
        if (!isSimpleObject(obj)) {
            for (Object o: visitedObjectsForGetters) {
                if (o == obj) {
                    return result;
                }
            }
            visitedObjectsForGetters.add(obj);
        }

        if (depth > Arguments.depth) {
            return result;
        }

        // Get the Class object of the instance
        Class<?> clazz = obj.getClass();

        // Get all methods of the class
        Method[] methods = clazz.getMethods();
        // sort the getter method names
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        // Iterate through the methods and invoke getters
        for (Method method : methods) {

            if (isValidMethod(method) && isGetter(method.getName()) && method.getParameterCount() == 0 && Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(clazz.getModifiers())) {
                // Invoke the getter method

                try {
                    Object value;
                    value = method.invoke(obj);
                    if (isComplexObjectType(value)) {
                        result += "\n" + getPrefix(depth) + method.getName() + " = complex";
                        String tempResult = getterState(value, depth + 1, "");
                        if (tempResult.equals("\n") || tempResult.equals("")) {

                        } else {
                            result = result +  "\n" + tempResult;
                        }
                    } else {
                        result += "\n" + getPrefix(depth) + method.getName() + " = " + maskNewLine(getText(value), depth);
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

    private synchronized static void writeFile( String path, List<String> output) {
        String s = String.join("\n",output);
        try {
            try (FileWriter writer = new FileWriter(path, true);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                bufferedWriter.write(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            addErrorMessage(getStackTraceInfo(e));
            System.err.println("Error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void writeInitializeInfo(ExtensionContext extensionContext) {
        generateDirs("initialize");
        appendToFile("initialize/stateNum.txt", testID + "---" + TestState.states.size());
        if (testClassName.contains(extensionContext.getTestClass().get().getName() + extensionContext.getTestMethod().get().getName())) {
            return;
        }
        testClassName.add(extensionContext.getTestClass().get().getName() + extensionContext.getTestMethod().get().getName());
        appendToFile("initialize/tests.txt", extensionContext.getTestClass().get().getName() + "-" + extensionContext.getTestMethod().get().getName());
    }

    public static void writeTimeInfo(String info) {
        generateDirs("initialize");
        appendToFile("initialize/time.txt", info);
    }



    public static void appendToFile(String filePath, String textToAppend) {
        try (FileWriter writer = new FileWriter(filePath, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            // Append the text
            bufferedWriter.write(textToAppend);
            bufferedWriter.newLine(); // This will add a new line after the text
        } catch (IOException e) {
            addErrorMessage(getStackTraceInfo(e));
            e.printStackTrace();
        }
    }


    public static Set<Integer> splitItems(int x, int y) {
        Set<Integer> indices = new TreeSet<>();

        if (y < 1) {
            indices.add(x);
            return indices;
        }

        // Calculate the step size to distribute indices evenly
        int stepSize = Math.max(1, x / y);

        //ensure the last index is included
        for (int i = x; i > 0 && indices.size() < y; i -= stepSize) {
            indices.add(i);
        }

        return indices;
    }

    public static List<String> getSimplifiedList(List<String> originalList) {
        List<String> simplifiedList = new ArrayList<>();

        for (String item : originalList) {
            if (!"\n".equals(item) || simplifiedList.isEmpty() || !"\n".equals(simplifiedList.get(simplifiedList.size() - 1))) {
                simplifiedList.add(item);
            }
        }
        return simplifiedList;
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
