package org.instrumentforsubject;

import org.instrumentation.group0instrumentor.InheritanceRelationships;
import org.staticsandpackage.DomainManagement;
import org.staticsandpackage.StaticFieldScanner;
import org.staticsandpackage.StaticInfoRecorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.instrumentation.annotation.AnnotationInstrumentor.instrumentAnnotation;
import static org.instrumentation.group0instrumentor.Instrumentor.processFiles;

/**
 *
 * This class is responsible for inserting helper packages to subject project for instrumentation
 */
public class DoInstrumentation {

    public static Scope scope;

    public static int numGetField = 0;

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Please provide the path to the target classes and the scope to be instrumented");
        }
        if (!args[1].equals("project") && !args[1].equals("package") && !args[1].equals("class") && !args[1].equals("all")) {
            throw new IllegalArgumentException("Please provide the scope to be instrumented: project, package, class, or all");
        }
        switch (args[1]) {
            case "project":
                scope = Scope.PROJECT;
                break;
            case "package":
                scope = Scope.PACKAGE;
                break;
            case "class":
                scope = Scope.CLASS;
                break;
            case "all":
                scope = Scope.ALL;
                break;
        }
        String path = args[0];
        doGroup0Instrumentation(path);
//        System.err.println(numGetField);
    }

    public static void doGroup0Instrumentation(String path) {
        System.err.println("hahah: " + path);
        // 0. clear the static fields
        StaticInfoRecorder.clearStaticFields();
        Path sourcePath = getPathForInstrumentation("classes");

        StaticFieldScanner.scanStatics(sourcePath.toFile());
        DomainManagement.getProjectDomain(DomainManagement.domains);
        processFiles(new File(path));

    }

    public static Path getPathForInstrumentation(String source) {
        // Project root (change if needed)
        File projectRoot = new File(System.getProperty("user.dir"));

        // target directory
        File targetDir = new File(projectRoot, "target");

        if (targetDir.exists() && targetDir.isDirectory()) {
            File[] matches = targetDir.listFiles(f -> f.isDirectory() && f.getName().startsWith(source));

            if (matches != null && matches.length > 0) {
                // Get the first match
                return matches[0].toPath();
//                System.out.println("Found: " + matches[0].getAbsolutePath());
            } else {
//                System.out.println("No matching directory found.");
            }
            matches = targetDir.listFiles(f -> f.isDirectory() && f.getName().startsWith("build"));
            if (matches != null && matches.length > 0) {
                // Get the first match
                return matches[0].toPath();
//                System.out.println("Found: " + matches[0].getAbsolutePath());
            } else {
//                System.out.println("No matching directory found.");
            }


        } else if (new File("build").exists()) {
            return new File("build").toPath();
        } else {
            System.out.println("'target/build' folder not found.");
        }
        return null;
    }

}
