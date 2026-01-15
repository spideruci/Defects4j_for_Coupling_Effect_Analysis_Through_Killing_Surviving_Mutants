package org.helper;

import org.helper.graph.Arguments;
import org.helper.graph.Graph;
import org.helper.graph.TesExporter;
import org.helper.states.Loc;
import org.helper.states.StateItem;
import org.helper.states.type;

import java.io.*;
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
import static org.helper.graph.Graph.*;

/**
 * This helper class/package is ideally instrumented into the subject project to record the states of the test execution
 */
public class TesExtension {

    public static org.helper.states.TesState states = new org.helper.states.TesState();

    public static Set<Integer> dumpingTiming = new HashSet<Integer>();

    public static String dir = "temp";

    public static String f = "statefile";

    public static boolean inRecording = false;

    public static Object thisClass;

    public static String testID = "nothing";

    private static long t0;

    private static Set<String> testClassName = new HashSet<String>();

    private static final List<String> forbiddenMethods;

    private static final List<String> forbiddenClasses;

    private static final List<String> forbiddenPackages;

    public static final List<Object> visitedObjectsForGetters = new ArrayList<Object>();

    static {
        forbiddenMethods = new ArrayList<String>();
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

        forbiddenClasses = new ArrayList<String>();
        forbiddenClasses.add("java.lang.Object");
        forbiddenClasses.add("java.lang.Class");
        forbiddenClasses.add("java.lang.Enum");
        forbiddenClasses.add("java.util.Date");
        forbiddenClasses.add("java.net.URL");
        forbiddenClasses.add("java.util.Calendar");
        forbiddenClasses.add("java.io.File");

        forbiddenPackages = new ArrayList<String>();
        forbiddenPackages.add("java.time");
    }

    public static void onTestMethodStart() {
        // TODO
        // handle for parameterized test
        states.clear();
        inRecording = true;
//        testID = extensionContext.getUniqueId();
//        if (extensionContext.getTestInstance().isPresent()) {
//            thisClass = extensionContext.getTestInstance().get();
//        }
//        if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent()) {
//            dir = extensionContext.getTestClass().get().getName();
//            f = extensionContext.getTestMethod().get().getName() + "_" + extensionContext.getUniqueId().hashCode();
//            if (stateNumMap.containsKey(extensionContext.getUniqueId()) && !isInitialScan && Arguments.multiDump) {
//                int totalNum = stateNumMap.get(extensionContext.getUniqueId());
//                dumpingTiming = splitItems(totalNum, Arguments.numDumps);
//            }
//        }
//        System.err.println(extensionContext.getUniqueId() + " before each");


    }

    public static void onTestMethodEnd() {
        dumpStates(dir, f, Arguments.depth, 1);
        states.clear();
        //        if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent() && !isInitialScan) {
////            String dir = extensionContext.getTestClass().get().getName();
////            String f = extensionContext.getTestMethod().get().getName() + "_" + extensionContext.getUniqueId().hashCode();
////            dumpStates(dir, f, Arguments.depth);
//        } else if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent() && isInitialScan) {
//            writeInitializeInfo(extensionContext);
//        }
//        states.clear();
//        dir = "";
//        f = "";
//        dumpingTiming.clear();
//        inRecording = false;
////        long t1 = System.nanoTime() - t0;
////        writeTimeInfo(extensionContext.getUniqueId() + "---" + t1);
//        System.err.println(extensionContext.getUniqueId() + " after each");

    }


//    /**
//     * stop recording states related to the test execution
//     * @param extensionContext
//     * @throws Exception
//     */
//    @Override
//    public void afterEach(ExtensionContext extensionContext) throws Exception {
//        if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent() && !isInitialScan) {
////            String dir = extensionContext.getTestClass().get().getName();
////            String f = extensionContext.getTestMethod().get().getName() + "_" + extensionContext.getUniqueId().hashCode();
////            dumpStates(dir, f, Arguments.depth);
//        } else if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent() && isInitialScan) {
//            writeInitializeInfo(extensionContext);
//        }
//        states.clear();
//        dir = "";
//        f = "";
//        dumpingTiming.clear();
//        inRecording = false;
////        long t1 = System.nanoTime() - t0;
////        writeTimeInfo(extensionContext.getUniqueId() + "---" + t1);
//        System.err.println(extensionContext.getUniqueId() + " after each");
//    }



//    /**
//     * start recording states related to the test execution
//     * @param extensionContext
//     * @throws Exception
//     */
//    @Override
//    public void beforeEach(ExtensionContext extensionContext) throws Exception {
////        t0 = System.nanoTime();
//        states.clear();
//        inRecording = true;
//        testID = extensionContext.getUniqueId();
//        if (extensionContext.getTestInstance().isPresent()) {
//            thisClass = extensionContext.getTestInstance().get();
//        }
//        if (extensionContext.getTestClass().isPresent() && extensionContext.getTestMethod().isPresent()) {
//            dir = extensionContext.getTestClass().get().getName();
//            f = extensionContext.getTestMethod().get().getName() + "_" + extensionContext.getUniqueId().hashCode();
//            if (stateNumMap.containsKey(extensionContext.getUniqueId()) && !isInitialScan && Arguments.multiDump) {
//                int totalNum = stateNumMap.get(extensionContext.getUniqueId());
//                dumpingTiming = splitItems(totalNum, Arguments.numDumps);
//            }
//        }
//        System.err.println(extensionContext.getUniqueId() + " before each");
//    }

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




        for (int i = 0; i < org.helper.states.TesState.states.size(); i++) {
            StateItem s = org.helper.states.TesState.states.get(i);
            output.add("*Meta " + s.getLine() + ": " + s.getSource() + " -> " + maskNewLine(s.getContent(),1));
            if (s.getObject() != null) {
                Graph g = new Graph(s.getObject(), depth);

                output.add(g.getString(g.getRoot(), Arguments.depth).substring(1));
            }
        }

        // add this if necessary
        if (Arguments.dumpThis) {
            StateItem thisClass = new StateItem(new Loc("this", "this"), type.Object, "", TesExtension.thisClass, 0);
            output.add("*Meta " + thisClass.getLine() + ": " + thisClass.getSource() + " -> " + maskNewLine(thisClass.getContent(),1));
            if (thisClass.getObject() != null) {
                Graph g = new Graph(thisClass.getObject(), depth);
                output.add(g.getString(g.getRoot(), Arguments.depth).substring(1));
            }
        }


        // add static fields if necessary
        if (Arguments.dumpStatic) {
            for (String content: org.helper.states.TesState.staticFields) {
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

        output.add("\n");
        writeFile(path, output);


        StateItem thisClass = new StateItem(new Loc("this", "this"), type.Object, "", TesExtension.thisClass, 0);
        Graph g = new Graph(thisClass.getObject(), depth);


        try {
            String json = TesExporter.exportRun(org.helper.states.TesState.states, true, g.getRoot(),true, new ArrayList<String>(org.helper.states.TesState.staticFields), Arguments.depth, 5);
//            System.out.println(doc);
            Path out = Paths.get("stateData", "temp", "statefile.json");
            Files.createDirectories(out.getParent());

            File outFile = new File("stateData/temp/statefile.json");
            outFile.getParentFile().mkdirs(); // create directories if they don’t exist

            BufferedWriter w = null;
            try {
                w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
                w.write(json);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (w != null) {
                    try {
                        w.close();
                    } catch (IOException ignore) {}
                }
            }


//            try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
//                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//                w.write(json);
//            }

        } catch (Exception e) {
            System.err.println("Failed to export: " + e.getMessage());
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
        Arrays.sort(methods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
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

    private static synchronized void writeFile(String path, List<String> output) {
        // Join lines manually
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < output.size(); i++) {
            sb.append(output.get(i));
            if (i < output.size() - 1) {
                sb.append("\n");
            }
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(path, true));
            bufferedWriter.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            addErrorMessage(getStackTraceInfo(e));
            System.err.println("Error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    //TODO
    public static void writeInitializeInfo() {
//        generateDirs("initialize");
//        appendToFile("initialize/stateNum.txt", testID + "---" + TestState.states.size());
//        if (testClassName.contains(extensionContext.getTestClass().get().getName() + extensionContext.getTestMethod().get().getName())) {
//            return;
//        }
//        testClassName.add(extensionContext.getTestClass().get().getName() + extensionContext.getTestMethod().get().getName());
//        appendToFile("initialize/tests.txt", extensionContext.getTestClass().get().getName() + "-" + extensionContext.getTestMethod().get().getName());
    }

    public static void writeTimeInfo(String info) {
        generateDirs("initialize");
        appendToFile("initialize/time.txt", info);
    }


    public static void appendToFile(String filePath, String textToAppend) {
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter writer = new FileWriter(filePath, true); // append = true
            bufferedWriter = new BufferedWriter(writer);
            // Append the text
            bufferedWriter.write(textToAppend);
            bufferedWriter.newLine(); // Add a new line after the text
        } catch (IOException e) {
            addErrorMessage(getStackTraceInfo(e));
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static Set<Integer> splitItems(int x, int y) {
        Set<Integer> indices = new TreeSet<Integer>();

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
        List<String> simplifiedList = new ArrayList<String>();

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
