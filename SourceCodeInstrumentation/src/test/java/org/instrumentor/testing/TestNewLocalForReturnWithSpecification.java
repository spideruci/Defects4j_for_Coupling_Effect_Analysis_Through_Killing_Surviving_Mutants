package org.instrumentor.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.instrumentor.Main;
import org.helper.Spec;
import org.instrumentor.TestFileLocator;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;


import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNewLocalForReturnWithSpecification {

    private Path DEMO_PATH;
    private Path RECORD_DIR;
    private Path INSTRUMENTED_SRC_DIR;
    private Path OUTPUT_DIR;
    private String DEMO_CLASS;
    private String SIMPLE_CLASS_NAME;
    private boolean isConstructor = false;
    private String CLASS_FQN;
    private String TARGET_METHOD;
    private int TARGET_LINE;
    private int TARGET_NTH;
    private String PROVIDED_TYPE;

//    @BeforeEach
//    public void setup() throws Exception {
//
//    }





    @Test
    public void testInstrumentationOnDemo1() throws Exception {
        for (File file: new File("oracle specification").listFiles()) {
            if (file.getName().equals(".DS_Store")) continue;
            System.out.println("--- Resetting variable counter ---");
            getOracleSpecification(file);
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
                System.err.println("instrumenting constructor");
                System.err.println(SIMPLE_CLASS_NAME);
                System.err.println(TARGET_LINE);
                System.err.println(TARGET_NTH);
                System.err.println(PROVIDED_TYPE);
                cu = (CompilationUnit) cu.accept(
                        Main.addConstructorVariableAt(TARGET_METHOD, TARGET_LINE, TARGET_NTH, PROVIDED_TYPE), null);
            } else {
                cu = (CompilationUnit) cu.accept(
                        Main.addReturnVariableAt(TARGET_METHOD, TARGET_LINE, TARGET_NTH, "", PROVIDED_TYPE), null);
            }
//        // Then instrument constructor Foo()
//        cu = (CompilationUnit) cu.accept(
//                Main.addConstructorVariableAt("Foo", 7, 0,"org.instrumentor.codeexamples.Foo"), null);

            String instrumentedSource = "// Instrumented at " + timestamp + "\n" + cu.toString();
            Files.write(afterFile, instrumentedSource.getBytes());

            System.out.println("Original saved to: " + beforeFile);
            System.out.println("Instrumented saved to: " + afterFile);

            // --- Step 5: Verify instrumentation ---
            assertTrue(instrumentedSource.contains("__ins_v1"),
                    "Expected instrumented variable not found");
//        assertTrue(instrumentedSource.contains("__ins_v2"),
//                "Expected instrumented variable not found");

            // --- Step 6: Compile instrumented file ---
            assertCompile(afterFile);

            // --- Step 7: Load and run test method ---
            runInstrumentedDemo();
        }
    }

    // --- Helper: compile source ---
    private void assertCompile(Path javaFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null,
                "-d", OUTPUT_DIR.toString(),
                javaFile.toString());
        assertTrue(result == 0, "Compilation failed for " + javaFile);
    }

    // --- Helper: load class and run test() ---
    private void runInstrumentedDemo() throws Exception {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{OUTPUT_DIR.toUri().toURL()})) {
            Class<?> demoClass = classLoader.loadClass(CLASS_FQN);
            Object demoInstance = demoClass.getDeclaredConstructor().newInstance();
            demoClass.getDeclaredMethod("test").invoke(demoInstance);
        }
    }

    public void getOracleSpecification(File file) {
        ObjectMapper mapper = new ObjectMapper();

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
            if (PROVIDED_TYPE.endsWith("." + TARGET_METHOD)) {
                System.err.println("hahahaha");
                isConstructor = true;
            } else {
                isConstructor = false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read specification file", e);
        }





    }
}

