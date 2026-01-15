package org.instrumentor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

    public static boolean isAssignableFrom(String subclass, String superclass) {
        subclass.replace(".","/");
        superclass.replace(".","/");
        String current = subclass;
        while (current != null && !current.equals("java/lang/Object")) {
            if (superclass.equals(current)) {
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


    public static List<File> listFilesRecursive(File dir) {
        List<File> result = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                result.add(file);
                if (file.isDirectory()) {
                    result.addAll(listFilesRecursive(file)); // recursion
                }
            }
        }
        return result;
    }



    public static void processFiles(File fileOrDir) {
        System.err.println("analyze inheritance relationships");
        System.err.println("Entering: " + fileOrDir.getAbsolutePath());
        if (Files.isDirectory(Paths.get(fileOrDir.getAbsolutePath()))) {
            List<File> files = listFilesRecursive(fileOrDir);
            if (files != null) {
                //inheritance analysis
                for (File file: files) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        if (Utils.containsIllegal(file.getAbsolutePath())) {
                            continue;
                        }
                        try {
                            // scan the class inheritance Relationships
                            // perform static analysis here to scan the instruction sequences
                            // scan local variables
                            ClassWriter writer = new MyClassWriter(ClassWriter.COMPUTE_FRAMES);
                            ClassReader reader = new ClassReader(new FileInputStream(file));
                            reader.accept(new InheritanceClassVisitor(writer), ClassReader.EXPAND_FRAMES);

                        } catch (IOException e) {

                        }
                    }
                }
            } else {
               System.err.println("Directory not found or empty.");
            }
        } else {
            throw new IllegalArgumentException("The path specified is neither a file nor a directory.");
        }
    }




}
