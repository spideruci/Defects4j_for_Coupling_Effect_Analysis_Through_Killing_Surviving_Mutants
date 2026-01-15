package org.staticsandpackage;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static Set<String> primitiveDes = new HashSet<>();

    public static String getStackTraceInfo(Exception e) {
        StringBuilder sb = new StringBuilder();
        if (e.getLocalizedMessage()!= null)
            sb.append(e.getLocalizedMessage());
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static boolean isPrimitive(String desc) {
        if (primitiveDes.contains(desc)) {
            return true;
        }
        return false;
    }
    static {
        primitiveDes.add("J");
        primitiveDes.add("D");
        primitiveDes.add("I");
        primitiveDes.add("B");
        primitiveDes.add("C");
        primitiveDes.add("S");
        primitiveDes.add("Z");
    }

    public static boolean containsIllegal(String s) {
        for (String illegal : illegalClasses) {
            if (s.contains(illegal)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> illegalClasses = new HashSet<>();
    static {
        illegalClasses.add("org/helper/graph");
        illegalClasses.add("org/helper/states");
        illegalClasses.add("org/helper/InstrumentationUtils");
        illegalClasses.add("org/helper/Utils");
        illegalClasses.add("org/helper/TestExtension");
        illegalClasses.add("org/helper/TestRunner");
        illegalClasses.add("auxiliary/");
        illegalClasses.add("ProxyAssertions/");
    }
}
