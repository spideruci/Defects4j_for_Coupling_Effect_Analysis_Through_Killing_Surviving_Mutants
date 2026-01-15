package org.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import org.instrumentor.Main;
import org.instrumentor.TestFileLocator;
import com.github.javaparser.ast.CompilationUnit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.instrumentor.Main.addConstructorVariableAt;
import static org.instrumentor.Main.addReturnVariableAt;

public class AssertionsForTesting {
    public static void verify(String message, Object obj) {
        // 1. serialize obj to json;
//        StateItem stateitem = new StateItem(new Loc("o", "o"), type.Object, "", obj, 0);
//        try {
//            String json = TesExporter.exportVar(stateitem,  5, 5);
//            writeActualOutputToFile(json);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("failed to serialize the object in assertions",e);
//        }

        // 2. extract the corresponding node from the access path for the obj.

        // TODO 2 [Run the python code] extract the corresponding node from the access path for the obj.
        // TODO 3 [Run the python code] extract the proper json node from the pre-recorded state file on fixed version of the code
        // and verify the the json with the message.
    }



    private static Path DEMO_PATH;
    private static Path RECORD_DIR;
    private static Path INSTRUMENTED_SRC_DIR;
    private static Path OUTPUT_DIR;
    private static String DEMO_CLASS;
    private static String SIMPLE_CLASS_NAME;
    private static boolean isConstructor = false;
    private static String CLASS_FQN;
    private static String TARGET_METHOD;
    private static int TARGET_LINE;
    private static int TARGET_NTH;
    private static String PROVIDED_TYPE;



    public static void main(String[] args) throws Exception {
        System.out.println("--- Resetting variable counter ---");
        getOracleSpecification();
        Main.fresh = new AtomicInteger(1);
        DEMO_PATH = Paths.get(DEMO_CLASS);
//                Paths.get("src", "test", "java",
//                PACKAGE.replace(".", "/"));
        RECORD_DIR = Paths.get("target", "instrumentation-records");
        Paths.get("target", "instrumented-sources",
                CLASS_FQN.replace(".", "/"));
        OUTPUT_DIR = Paths.get("target", "instrumented-classes");

        INSTRUMENTED_SRC_DIR = Paths.get("target", "instrumented-sources",
                CLASS_FQN.replace(".", "/"));
        OUTPUT_DIR = Paths.get("target", "instrumented-classes");

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // --- Step 1: Read source ---
        String originalSource = new String(Files.readAllBytes(DEMO_PATH));

        // --- Step 2: Prepare output dirs ---
        Files.createDirectories(RECORD_DIR);
        Files.createDirectories(INSTRUMENTED_SRC_DIR);
        Files.createDirectories(OUTPUT_DIR);

//        System.err.println(DEMO_CLASS);

        // Split DEMO_CLASS by "/" and get the last part
        String[] parts = DEMO_CLASS.split("/");
        String lastPart = parts[parts.length - 1];  // equivalent to [-1] in Python

        // Remove the extension (split by "." and take first part)
        String baseName = lastPart.split("\\.")[0];  // need to escape "." in regex

        // Build paths
        Path beforeFile = RECORD_DIR.resolve(baseName + "_before.java");
//        System.err.println(beforeFile);

        Path afterFile = INSTRUMENTED_SRC_DIR.resolve(baseName + ".java");
//        System.err.println(afterFile);

        // --- Step 3: Save original source with timestamp ---
        Files.write(beforeFile, ("// Saved at " + timestamp + "\n" + originalSource).getBytes());

        // --- Step 4: Instrument source ---
        CompilationUnit cu = StaticJavaParser.parse(originalSource);
        if (isConstructor) {
            System.err.println("jj");
            cu = (CompilationUnit) cu.accept(
                    addConstructorVariableAt(SIMPLE_CLASS_NAME, TARGET_LINE, TARGET_NTH, PROVIDED_TYPE), null);
        } else {
            cu = (CompilationUnit) cu.accept(
                    addReturnVariableAt(TARGET_METHOD, TARGET_LINE, TARGET_NTH, "", PROVIDED_TYPE), null);
        }
//        // Then instrument constructor Foo()
//        cu = (CompilationUnit) cu.accept(
//                Main.addConstructorVariableAt("Foo", 7, 0,"org.instrumentor.codeexamples.Foo"), null);

        String instrumentedSource = "// Instrumented at " + timestamp + "\n" + cu.toString();
        Files.write(afterFile, instrumentedSource.getBytes());

        System.out.println("Original saved to: " + beforeFile);
        System.out.println("Instrumented saved to: " + afterFile);


    }

    public static void getOracleSpecification() {
        ObjectMapper mapper = new ObjectMapper();

        // Adjust the file path
        File file = new File("oracle specification/assertion_12.json");

        try {
            Spec spec = mapper.readValue(file, Spec.class);
            System.out.println("Loaded spec:");
            System.out.println(spec);
            DEMO_CLASS = String.valueOf(TestFileLocator.locateTestFile(spec.test_name));
            CLASS_FQN = spec.test_name.split("::")[0];
            TARGET_METHOD = spec.name;
            TARGET_LINE = spec.line_number;
            TARGET_NTH = spec.ordinal;
            PROVIDED_TYPE = spec.returnType;
            SIMPLE_CLASS_NAME = spec.simple_class_name;
            if (TARGET_METHOD.equals(PROVIDED_TYPE)) {
                isConstructor = true;
            } else {
                isConstructor = false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read specification file", e);
        }





    }


    public static void writeActualOutputToFile(String output) {
        // TODO write the output to the file

        try {
            String json = output;
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

        } catch (Exception e) {
            System.err.println("Failed to export: " + e.getMessage());
        }
    }

}
