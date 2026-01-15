package org.instrumentation.group0instrumentor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InheritanceRelationships {

    // key: internal class name (e.g., "com/example/MyTest")
    // value: internal super class name (e.g., "junit/framework/TestCase")
    private static final Map<String, String> inheritanceMap = new HashMap<>();

    public static void record(String className, String superName) {
        inheritanceMap.put(className, superName);
    }

    public static Map<String, String> getAllRelationships() {
        return Collections.unmodifiableMap(inheritanceMap);
    }

    public static boolean extendsJUnit3(String className) {
        String current = className;
        while (current != null && !current.equals("java/lang/Object")) {
            if ("junit/framework/TestCase".equals(current)) {
                return true;
            }
            current = inheritanceMap.get(current);
        }
        return false;
    }

    public static boolean prettyPrintInheritanceRelationships() {
        if (inheritanceMap.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, String> entry : inheritanceMap.entrySet()) {
            System.out.printf("Class: %s extends %s%n", entry.getKey(), entry.getValue());
        }
        return true;
    }

}
