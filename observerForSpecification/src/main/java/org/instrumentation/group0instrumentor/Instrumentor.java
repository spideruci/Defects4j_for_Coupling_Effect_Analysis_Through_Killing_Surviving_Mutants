package org.instrumentation.group0instrumentor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Instrumentor {

    public static void instrumentTestClass(File file) throws IOException {
        System.err.println("instrument " + file.getName());
        // perform static analysis here to scan the instruction sequences
        // scan local variables
        ClassWriter writer = new MyClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassReader reader = new ClassReader(new FileInputStream(file));
        reader.accept(new VariableTrackingClassVisitor(Opcodes.ASM9,writer), ClassReader.EXPAND_FRAMES);


        //do STATE instrumentation
        FileInputStream fis = new FileInputStream(file);
        ClassReader cr = new ClassReader(fis);
        ClassWriter cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new AssertionOverwritter(Opcodes.ASM9, cw);
//
//        cr.accept(cv, 0);

//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(cw.toByteArray());
//
//        fis.close();
//        fos.close();


        // do instrumentation here to dump program states
//        fis = new FileInputStream(file);
//        cr = new ClassReader(fis);

        cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES);
        cv = new StateMonitorClassVisitor(cw);
        cr.accept(cv, 0);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(cw.toByteArray());

        fis.close();
        fos.close();
    }

    public static List<File> listFilesRecursive(File dir) {
        List<File> result = new ArrayList<>();
        System.err.println(dir.getAbsolutePath());
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
        System.err.println("Entering: " + fileOrDir.getAbsolutePath());

        if (Files.isDirectory(Paths.get(fileOrDir.getAbsolutePath()))) {
            List<File> files = listFilesRecursive(fileOrDir);

            try {
                if (files != null) {
                    //inheritance analysis
                    for (File file : files) {
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

                            } catch (Throwable e) {
                                e.printStackTrace();
//                            System.err.println("Inheritance Analysis failure: " + file.getName());
                            }
                        }
                    }
                    for (File file : files) {
                        if (file.getAbsolutePath().contains("target/classes")) {
                            continue;
                        }
                        if (file.getAbsolutePath().contains("build/classes")) {
                            continue;
                        }
                        if (file.getAbsolutePath().contains("build") && file.getAbsolutePath().contains("build/classes")) {
                            continue;
                        }
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            if (Utils.containsIllegal(file.getAbsolutePath())) {
                                continue;
                            }
                            try {
//                            System.err.println("Instrumenting: " + file.getName());
                                instrumentTestClass(file);
//                            System.err.println("Instrumented: " + file.getName());
                            } catch (Throwable e) {
                                e.printStackTrace();
//                            System.err.println("Instrumentation failure: " + file.getName());
                            }
                        } else if (file.isDirectory()) {
                            continue;
//                        processFiles(file); // Recursively process subdirectories
                        }
                    }
                } else {
                }
            } catch (Throwable t) {
                System.err.println("State Instrumentation Failure");
                t.printStackTrace();
            }

        }
//        } else if (Files.isRegularFile(Paths.get(fileOrDir.getAbsolutePath()))) {
//            if (Utils.containsIllegal(fileOrDir.getAbsolutePath())) {
//                return;
//            }
//            try {
////                System.err.println("Instrumenting: " + fileOrDir.getName());
//                instrumentTestClass(fileOrDir);
////                System.err.println("Instrumented: " + fileOrDir.getName());
//            } catch (IOException e) {
////                System.err.println("Instrumentation failure: " + fileOrDir.getName());
//            }
//        } else {
//            throw new IllegalArgumentException("The path specified is neither a file nor a directory.");
//        }
    }



//    public static void processFilesForInheritanceAnalysis(File fileOrDir) {
//        System.err.println("jjjj1");
//        if (Files.isDirectory(Paths.get(fileOrDir.getAbsolutePath()))) {
//            File[] files = fileOrDir.listFiles();
//            System.err.println(files.length);
//            System.err.println(fileOrDir.getName());
//            if (files != null) {
//                for (File file : files) {
//                    System.err.println("jjjj2");
//                    System.err.println(file.getName());
//                    if (file.isFile() && file.getName().endsWith(".class")) {
//                        System.err.println("jjjj3");
//                        if (Utils.containsIllegal(file.getAbsolutePath())) {
//                            System.err.println(file.getAbsolutePath());
//                            continue;
//                        }
//                        System.err.println("jjjj4");
//                        try {
//                            // scan the class inheritance Relationships
//                            // perform static analysis here to scan the instruction sequences
//                            // scan local variables
//                            System.err.println("jjjj");
//                            ClassWriter writer = new MyClassWriter(ClassWriter.COMPUTE_FRAMES);
//                            ClassReader reader = new ClassReader(new FileInputStream(file));
//                            reader.accept(new InheritanceClassVisitor(writer), ClassReader.EXPAND_FRAMES);
//
//                        } catch (IOException e) {
//                            System.err.println("Inheritance Analysis failure: " + file.getName());
//                        }
//                    } else if (file.isDirectory()) {
//                        processFiles(file); // Recursively process subdirectories
//                    }
//                }
//            } else {
//                System.err.println("Directory not found or empty.");
//            }
//        } else if (Files.isRegularFile(Paths.get(fileOrDir.getAbsolutePath()))) {
//            if (Utils.containsIllegal(fileOrDir.getAbsolutePath())) {
//                return;
//            }
//            processFilesForInheritanceAnalysis(fileOrDir);
//        } else {
//            System.err.println(fileOrDir.getName());
//            throw new IllegalArgumentException("The path specified is neither a file nor a directory.");
//        }
//    }
}
