package org.instrumentation.annotation;

import org.instrumentation.group0instrumentor.MyClassWriter;
import org.instrumentation.group0instrumentor.Utils;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AnnotationInstrumentor {

    private static void instrumentTestClass(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ClassReader cr = new ClassReader(fis);
        ClassWriter cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new TestClassVisitor(cw);

        cr.accept(cv, 0);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(cw.toByteArray());

        fis.close();
        fos.close();
    }

    static class TestClassVisitor extends ClassVisitor {
        private boolean extendWithExists = false;

        public TestClassVisitor(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            // Check if the ExtendWith annotation is already present
            if (Type.getDescriptor(org.junit.jupiter.api.extension.ExtendWith.class).equals(desc)) {
                extendWithExists = true;
                return new ExtendWithAnnotationVisitor(super.visitAnnotation(desc, visible));
            }
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public void visitEnd() {
            if (!extendWithExists) {
                // Add ExtendWith annotation if it does not exist
                AnnotationVisitor av = super.visitAnnotation(Type.getDescriptor(org.junit.jupiter.api.extension.ExtendWith.class), true);
                av = av.visitArray("value");
                av.visit(null, Type.getType("Lorg/helper/TestExtension;"));
                av.visitEnd();
            }
            super.visitEnd();
        }

        // Inner class to handle modification of the ExtendWith annotation
        class ExtendWithAnnotationVisitor extends AnnotationVisitor {
            public ExtendWithAnnotationVisitor(AnnotationVisitor av) {
                super(Opcodes.ASM9, av);
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                AnnotationVisitor av = super.visitArray(name);
                // Add new value to the existing array of values in the ExtendWith annotation
                if ("value".equals(name)) {
                    av.visit(null, Type.getType("Lorg/helper/TestExtension;"));
                }
                return av;
            }
        }
    }

    public static void instrumentAnnotation(String path) {
        processFiles(new File(path));
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
//                            System.err.println("start instrumenting: " + file.getName());
                            instrumentTestClass(file);
//                            System.err.println("Instrumented: " + file.getName());
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
//                    System.err.println("start instrumenting: " + fileOrDir.getName());
                    instrumentTestClass(fileOrDir);
//                    System.err.println("Instrumented: " + fileOrDir.getName());
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.err.println("Instrumentation failure: " + fileOrDir.getName());
                }
            }
        } else {
            throw new IllegalArgumentException("The path specified is neither a file nor a directory.");
        }
    }

}
