package org.staticsandpackage;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class StaticFieldScanner {

    public static void scanStatics(File fileOrDir) {
        processFiles(fileOrDir);
        StaticInfoRecorder.writeFieldsToFile();
    }

    public static void processFiles(File fileOrDir) {

        if (Files.isDirectory(Paths.get(fileOrDir.getAbsolutePath()))) {
            File[] files = fileOrDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // this package is for instrumentation
                    if (Utils.containsIllegal(file.getAbsolutePath())) {
                        continue;
                    }
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        try {
                            scanStaticFields(file);
//                            System.out.println("Instrumented: " + file.getName());
                        } catch (IOException e) {
//                            System.err.println("Instrumentation failure: " + file.getName());
                        }
                    } else if (file.isDirectory()) {
                        processFiles(file); // Recursively process subdirectories
                    }
                }
            } else {
//                System.err.println("Directory not found or empty.");
            }
        } else if (Files.isRegularFile(Paths.get(fileOrDir.getAbsolutePath()))) {
            if (!Utils.containsIllegal(fileOrDir.getAbsolutePath())) {
                try {
                    scanStaticFields(fileOrDir);
//                    System.out.println("Scan static Fields: " + fileOrDir.getName());
                } catch (IOException e) {
//                    System.err.println("Static Field scanning failure: " + fileOrDir.getName());
                }
            }
        } else {
            throw new IllegalArgumentException("The path specified is neither a file nor a directory.");
        }
    }


    private static void scanStaticFields(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ClassReader cr = new ClassReader(fis);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new StaticFieldClassVisitor(cw);

        cr.accept(cv, 0);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(cw.toByteArray());

        fis.close();
        fos.close();
    }
}
