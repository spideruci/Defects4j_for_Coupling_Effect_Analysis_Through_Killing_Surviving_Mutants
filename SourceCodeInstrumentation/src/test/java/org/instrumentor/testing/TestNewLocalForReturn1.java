package org.instrumentor.testing;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.instrumentor.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNewLocalForReturn1 {

    @BeforeEach
    public void setup() throws Exception {
        System.out.println("--- Resetting variable counter ---");
        Main.fresh = new AtomicInteger(1);
    }

    // --- Configurable parameters ---
    private static final String TEST_CLASS = "Demo1";
    private static final String PACKAGE = "org.instrumentor.codeexamples";
    private static final String TARGET_METHOD = "foo";
    private static final int TARGET_LINE = 7;
    private static final int TARGET_NTH = 0;
    private static final String PROVIDED_TYPE = PACKAGE + ".Foo"; // return value type (come from method signature)

    private static final Path TestCodePath = Paths.get("src", "test", "java",
            PACKAGE.replace(".", "/"), TEST_CLASS + ".java");
    private static final Path RECORD_DIR = Paths.get("target", "instrumentation-records");
    private static final Path INSTRUMENTED_SRC_DIR = Paths.get("target", "instrumented-sources",
            PACKAGE.replace(".", "/"));
    private static final Path OUTPUT_DIR = Paths.get("target", "instrumented-classes");

    @Test
    public void testInstrumentationOnDemo1() throws Exception {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // --- Step 1: Read source ---
        String originalSource = new String(Files.readAllBytes(TestCodePath));

        // --- Step 2: Prepare output dirs ---
        Files.createDirectories(RECORD_DIR);
        Files.createDirectories(INSTRUMENTED_SRC_DIR);
        Files.createDirectories(OUTPUT_DIR);

        Path beforeFile = RECORD_DIR.resolve(TEST_CLASS + "_before.java");
        Path afterFile = INSTRUMENTED_SRC_DIR.resolve(TEST_CLASS + ".java");

        // --- Step 3: Save original source with timestamp ---
        Files.write(beforeFile, ("// Saved at " + timestamp + "\n" + originalSource).getBytes());

        // --- Step 4: Instrument source ---
        CompilationUnit cu = StaticJavaParser.parse(originalSource);
        cu = (CompilationUnit) cu.accept(
                Main.addReturnVariableAt(TARGET_METHOD, TARGET_LINE, TARGET_NTH, "", PROVIDED_TYPE), null);
        // Then instrument constructor Foo()
        cu = (CompilationUnit) cu.accept(
                Main.addConstructorVariableAt("Foo", 7, 0,"org.instrumentor.codeexamples.Foo"), null);

        String instrumentedSource = "// Instrumented at " + timestamp + "\n" + cu.toString();
        Files.write(afterFile, instrumentedSource.getBytes());

        System.out.println("Original saved to: " + beforeFile);
        System.out.println("Instrumented saved to: " + afterFile);

        // --- Step 5: Verify instrumentation ---
        assertTrue(instrumentedSource.contains("__ins_v1"),
                "Expected instrumented variable not found");
        assertTrue(instrumentedSource.contains("__ins_v2"),
                "Expected instrumented variable not found");

        // --- Step 6: Compile instrumented file ---
        assertCompile(afterFile);

        // --- Step 7: Load and run test method ---
        runInstrumentedDemo();
    }

    @Test
    public void testInstrumentationOnDemo1_switch() throws Exception {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // --- Step 1: Read source ---
        String originalSource = new String(Files.readAllBytes(TestCodePath));

        // --- Step 2: Prepare output dirs ---
        Files.createDirectories(RECORD_DIR);
        Files.createDirectories(INSTRUMENTED_SRC_DIR);
        Files.createDirectories(OUTPUT_DIR);

        Path beforeFile = RECORD_DIR.resolve(TEST_CLASS + "_before.java");
        Path afterFile = INSTRUMENTED_SRC_DIR.resolve(TEST_CLASS + ".java");

        // --- Step 3: Save original source with timestamp ---
        Files.write(beforeFile, ("// Saved at " + timestamp + "\n" + originalSource).getBytes());

        // --- Step 4: Instrument source ---
        CompilationUnit cu = StaticJavaParser.parse(originalSource);



        cu = (CompilationUnit) cu.accept(
                Main.addConstructorVariableAt("Foo", 7, 0,"org.instrumentor.codeexamples.Foo"), null);
        cu = (CompilationUnit) cu.accept(
                Main.addReturnVariableAt(TARGET_METHOD, TARGET_LINE, TARGET_NTH, "", PROVIDED_TYPE), null);

        String instrumentedSource = "// Instrumented at " + timestamp + "\n" + cu.toString();
        Files.write(afterFile, instrumentedSource.getBytes());

        // --- Step 5: Verify instrumentation ---
        assertTrue(instrumentedSource.contains("__ins_v1"),
                "Expected instrumented variable not found");
        assertTrue(instrumentedSource.contains("__ins_v2"),
                "Expected instrumented variable not found");

        System.out.println("Original saved to: " + beforeFile);
        System.out.println("Instrumented saved to: " + afterFile);


        // --- Step 6: Compile instrumented file ---
        assertCompile(afterFile);

        // --- Step 7: Load and run test method ---
        runInstrumentedDemo();
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
            Class<?> demoClass = classLoader.loadClass(PACKAGE + "." + TEST_CLASS);
            Object demoInstance = demoClass.getDeclaredConstructor().newInstance();
            demoClass.getDeclaredMethod("test").invoke(demoInstance);
        }
    }
}
