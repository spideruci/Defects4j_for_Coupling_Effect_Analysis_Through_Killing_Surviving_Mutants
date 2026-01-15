package org.instrumentation.group0instrumentor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a bypass of failing to load specific classes defined in the project's test classes and will not be loaded during instrumentation
 */
public class MyClassWriter extends ClassWriter {
    public MyClassWriter(int flags) {
        super(flags);
    }

    public MyClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        Map<String, String> inheritanceMap = InheritanceRelationships.getAllRelationships();
        if (type1.equals(type2)) {
            return type1;
        }

        // Collect ancestors of type1
        Set<String> ancestors = new HashSet<>();
        String current = type1;
        while (current != null) {
            ancestors.add(current);
            current = inheritanceMap.get(current); // climb using your map
        }

        // Climb type2 until we hit a common ancestor
        current = type2;
        while (current != null) {
            if (ancestors.contains(current)) {
                return current; // found common ancestor
            }
            current = inheritanceMap.get(current);
        }

        // If nothing found, fall back to Object
        return "java/lang/Object";
//        return super.getCommonSuperClass(type1, type2);
    }
}