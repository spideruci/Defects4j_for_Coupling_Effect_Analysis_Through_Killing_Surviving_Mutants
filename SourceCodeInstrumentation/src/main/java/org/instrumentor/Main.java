package org.instrumentor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.helper.FixedStateUtils;
//import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassFileTypeSolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.helper.Spec;


/**
 * Known Issues:
 * 1) Line number reasoned by Java Bytecode through ASM may not be 100% accurate. As such, sometimes, JavaParser fails to find the variable from the code.
 */
public class Main {

    private static Path DEMO_PATH;
    private static Path RECORD_DIR;
    private static Path INSTRUMENTED_SRC_DIR;
    private static Path OUTPUT_DIR;
    private static String DEMO_CLASS;
    private static String SIMPLE_CLASS_NAME;
    private static boolean isConstructor = false;
    private static boolean isField = false;
    private static boolean isLocal = false;
    private static String CLASS_FQN;
    private static String TARGET_METHOD;
    private static String READABLE_ACCESS;
    private static int TARGET_LINE;
    private static int TARGET_NTH;
    private static String PROVIDED_TYPE;
    private static int testCounter = 0;
    private static String varName;
    private static String targetReceiverFqn;
    private static boolean isInLoop = false;
    private static int loop;
    private static int actual_targetLine = -1;
    private static String fixedStateFile = "";

    CombinedTypeSolver combined = new CombinedTypeSolver();

    private static void addSourceSolverIfExists(CombinedTypeSolver combinedSolver, String path) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
//            System.out.println("Adding source solver for: " + dir.getAbsolutePath());
            combinedSolver.add(new JavaParserTypeSolver(dir));
        } else {
//            System.out.println("Skipping non-existent source folder: " + dir.getAbsolutePath());
        }
    }

    public static void resolve() {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();

        // Always add JDK
        combinedSolver.add(new ReflectionTypeSolver());

        // Conditionally add sources
        addSourceSolverIfExists(combinedSolver, "src/main/java");
        addSourceSolverIfExists(combinedSolver, "src/test/java");
        addSourceSolverIfExists(combinedSolver, "src/java");
        addSourceSolverIfExists(combinedSolver, "src/test");
        //JfreeChart is using this style below
        addSourceSolverIfExists(combinedSolver, "source");
        addSourceSolverIfExists(combinedSolver, "tests");

        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedSolver));
        StaticJavaParser.setConfiguration(config);
    }


    public static AtomicInteger fresh = new AtomicInteger(1);


    public static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;

        // Walk the file tree in reverse order (delete children before parent)
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete: " + p, e);
                    }
                });
    }

    public static List<String> getAllFilePaths(File dir) {
        List<String> result = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getAllFilePaths(file));
            } else {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

    public static boolean flagRealBug = true;

    public static void main(String[] args) throws Exception {

        resolve();
        File file_oracle_folder = new File("oracle specification");
        if (args.length != 0 && args[0].contains("m")) {
            flagRealBug = false;
            deleteRecursively(Paths.get("oracles_mutants"));
            file_oracle_folder = new File("mutant_oracle_specification");
        } else {
            deleteRecursively(Paths.get("oracles"));
        }

        if (new File("target/").isDirectory()) {
            InheritanceRelationships.processFiles(new File("target/"));
        } else if (new File("build/").isDirectory()) {
            InheritanceRelationships.processFiles(new File("build/"));
            if (new File("build-tests/").isDirectory()) {
                InheritanceRelationships.processFiles(new File("build-tests"));
            }
        }

        for (String p: getAllFilePaths(file_oracle_folder)) {
            try {
                File file = new File(p);
                isConstructor = false;
                isField = false;
                isLocal = false;

                if (file.getName().equals(".DS_Store")) continue;
//            System.out.println("--- Resetting variable counter ---");
                getOracleSpecification(file);

                Main.fresh = new AtomicInteger(1);
                DEMO_PATH = Paths.get(DEMO_CLASS);

                if (flagRealBug) {
                    RECORD_DIR = Paths.get("oracles/before/");
                    INSTRUMENTED_SRC_DIR = Paths.get("oracles");
                } else {
                    RECORD_DIR = Paths.get("oracles_mutants/before/");
                    INSTRUMENTED_SRC_DIR = Paths.get("oracles_mutants");

                }
                Paths.get("target", "instrumented-sources",
                        CLASS_FQN.replace(".", "/"));
                OUTPUT_DIR = Paths.get("target", "instrumented-classes");

                OUTPUT_DIR = Paths.get("target", "instrumented-classes");

                String timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // --- Step 1: Read source ---
                String originalSource = new String(Files.readAllBytes(DEMO_PATH));

                // --- Step 2: Prepare output dirs ---
                Files.createDirectories(RECORD_DIR);
                Files.createDirectories(INSTRUMENTED_SRC_DIR);
                Files.createDirectories(OUTPUT_DIR);

                // Split DEMO_CLASS by "/" and get the last part
                String[] parts = DEMO_CLASS.split("/");
                String lastPart = parts[parts.length - 1];  // equivalent to [-1] in Python

                // Remove the extension (split by "." and take first part)
                String baseName = lastPart.split("\\.")[0];  // need to escape "." in regex

                // Build paths
                Path beforeFile = RECORD_DIR.resolve(baseName + "_before_" + CLASS_FQN + ".java");

                Path afterFile = INSTRUMENTED_SRC_DIR.resolve(testCounter + "__" + baseName + ".java");

                // --- Step 3: Save original source with timestamp ---
                Files.write(beforeFile, (originalSource).getBytes());

                // --- Step 4: Instrument source ---
                CompilationUnit cu = StaticJavaParser.parse(originalSource);
                if (isConstructor) {
                    cu = (CompilationUnit) cu.accept(
                            Main.addConstructorVariableAt(TARGET_METHOD, TARGET_LINE, TARGET_NTH, PROVIDED_TYPE), null);
                } else if (isField) {
                    cu = (CompilationUnit) cu.accept(
                            Main.addFieldCaptureAt(/* fieldName */ varName,
                                    /* line */      TARGET_LINE,
                                    /* nth */       TARGET_NTH,
                                    /* type */      PROVIDED_TYPE),
                            null);
                } else if (isLocal) {
                    cu = (CompilationUnit) cu.accept(
                            Main.addLocalCaptureAt(/* name */ varName,
                                    /* line */ TARGET_LINE,
                                    /* nth */ TARGET_NTH,
                                    /* type */ PROVIDED_TYPE), null);
                } else
                {
                    cu = (CompilationUnit) cu.accept(
                            Main.addReturnVariableAt(TARGET_METHOD, TARGET_LINE, TARGET_NTH, targetReceiverFqn, PROVIDED_TYPE), null);
                }

                String instrumentedSource = "// Instrumented at " + timestamp + "\n" + cu.toString();
                Files.write(afterFile, instrumentedSource.getBytes());

                System.out.println("Instrumented saved to: " + afterFile);

            } catch (Throwable t) {
                System.err.println("⚠ Failed to process file " + p);
                t.printStackTrace();
            }


        }

    }

    public static void getOracleSpecification(File file) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Spec spec = mapper.readValue(file, Spec.class);

            if (spec.loop >= 1) {
                isInLoop = true;
                loop = spec.loop;
            } else {
                isInLoop = false;
            }
            String mutant_id = "";
            if (!flagRealBug) {
                mutant_id = file.getAbsoluteFile().getParentFile().getName();
                System.err.println("mutant_id: " + mutant_id);
            }
            if (spec.source.equals("return")) {
                DEMO_CLASS = String.valueOf(TestFileLocator.locateTestFile(spec.test_name));
                CLASS_FQN = spec.test_name.split("::")[0];
                fixedStateFile = "all_states/" + spec.test_name + "/fixed/1.json";
                READABLE_ACCESS = spec.readable_access + "_" + file.getName().split("_")[1].split(".json")[0] + "_" + mutant_id;
                TARGET_METHOD = spec.name;
                TARGET_LINE = spec.line_number;
                TARGET_NTH = spec.ordinal;
                PROVIDED_TYPE = spec.returnType;

                if (PROVIDED_TYPE.equals("void")) {
                    PROVIDED_TYPE = spec.owner;
                }
                targetReceiverFqn = spec.owner;
                SIMPLE_CLASS_NAME = spec.simple_class_name;
                if (PROVIDED_TYPE.endsWith("." + TARGET_METHOD)) {
                    isConstructor = true;
                } else {
                    isConstructor = false;
                }
                testCounter = extractNumber(file.getName());
            } else if (spec.source.equals("getField")) {
                DEMO_CLASS = String.valueOf(TestFileLocator.locateTestFile(spec.test_name));
                CLASS_FQN = spec.test_name.split("::")[0];
                READABLE_ACCESS = spec.readable_access + "_" + file.getName().split("_")[1].split(".json")[0] + "_" + mutant_id;
                isField = true;
                PROVIDED_TYPE = spec.returnType;
                varName = spec.name;
                TARGET_NTH = spec.ordinal;
                TARGET_LINE = spec.line_number;
                testCounter = extractNumber(file.getName());
            } else if (spec.source.equals("local")) {
                DEMO_CLASS = String.valueOf(TestFileLocator.locateTestFile(spec.test_name));
                CLASS_FQN = spec.test_name.split("::")[0];
                READABLE_ACCESS = spec.readable_access + "_" + file.getName().split("_")[1].split(".json")[0] + "_" + mutant_id;
                isLocal = true;
                PROVIDED_TYPE = spec.owner;
                varName = spec.name;
                TARGET_NTH = spec.ordinal;
                TARGET_LINE = spec.line_number;
                testCounter = extractNumber(file.getName());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read specification file", e);
        }





    }

    public static int extractNumber(String filename) {
        Pattern p = Pattern.compile("assertion_(\\d+)\\.json");
        Matcher m = p.matcher(filename);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalArgumentException("Filename format not recognized: " + filename);
        }
    }

    public static FieldCaptureRewriter addFieldCaptureAt(String fieldName, int targetLine, int targetNth, String providedType) throws Exception {
        return new FieldCaptureRewriter(fieldName, targetLine, targetNth, providedType);
    }


    public static ChainCaptureRewriter addReturnVariableAt(String targetMethod, int targetLine, int targetNth, String targetReceiverFqn, String providedType) throws Exception {
        return new ChainCaptureRewriter(targetMethod,targetLine,targetNth,targetReceiverFqn,providedType);
    }

    public static ConstructorCaptureRewriter addConstructorVariableAt(String targetMethod, int targetLine, int targetNth, String targetReceiverFqn) throws Exception {
        return new ConstructorCaptureRewriter(targetMethod,targetLine,targetNth,targetReceiverFqn);
    }

    public static LocalCaptureRewriter addLocalCaptureAt(String name, int line, int nth, String providedType) throws Exception {
        return new LocalCaptureRewriter(name, line, nth, providedType);
    }



    // --- Inner class: the rewriter ---
    static class ChainCaptureRewriter extends ModifierVisitor<Void> {
        private final String targetMethod;
        private final int targetLine;
        private final int targetNth;
        private final String targetReceiverFqn;

        private final AtomicInteger fresh = new AtomicInteger(1);
        private MethodCallExpr targetCallExpr = null;
        private final String providedType;

        ChainCaptureRewriter(String targetMethod, int targetLine, int targetNth, String targetReceiverFqn,String providedType) {
            this.targetMethod = targetMethod;
            this.targetLine = targetLine;
            this.targetNth = targetNth;
            this.targetReceiverFqn = targetReceiverFqn;
            this.providedType = providedType;
        }


        @Override
        public Visitable visit(CompilationUnit cu, Void arg) {
            // Step 1: locate target calls on the given line
            List<MethodCallExpr> candidates = cu.findAll(MethodCallExpr.class).stream()
                    .filter(m -> m.getNameAsString().equals(targetMethod))
                    .filter(m -> m.getRange().map(r ->
                            targetLine >= r.begin.line && targetLine <= r.end.line
                    ).orElse(false))
                    .sorted(Comparator.comparingInt(m -> m.getRange().map(r -> r.begin.column).orElse(Integer.MAX_VALUE)))
                    .collect(Collectors.toList());

            // AST gives right-to-left for nested calls; reverse to align with textual left-to-right
            Collections.reverse(candidates);

            List<MethodCallExpr> filtered = candidates.stream().filter(call -> {
                try {
                    ResolvedMethodDeclaration rmd = call.resolve();

                    // Declaring type check
                    String declType = rmd.declaringType().getQualifiedName();
                    if (!targetReceiverFqn.isEmpty()) {
                        if (InheritanceRelationships.isAssignableFrom(targetReceiverFqn, declType)) {
                            return true;
                        }
                    } else {
                        return false;
                    }


                    // Return type check
                    String returnType = rmd.getReturnType().describe();
                    if ((returnType.length() == 1)) {
                        // heuristics to detect genertics.
                        return true;
                    }
                    if (PROVIDED_TYPE != null && !PROVIDED_TYPE.isEmpty() && !returnType.equals(PROVIDED_TYPE)) {
                        return false;
                    }

                    return true; // passes all requirements
                } catch (Throwable t) {
//                    System.err.println("⚠ Could not resolve " + call + ", skipping strict check.");
                    // unsafe, types cannot be resolved, but better than nothing
                    return true;
                }
            }).collect(Collectors.toList());

            System.err.println("Filtered down to " + filtered.size() + " candidates after type checks.");
            // Step 3: pick the Nth *after* filtering
            if (targetNth >= 0 && targetNth < filtered.size()) {
                targetCallExpr = filtered.get(targetNth);
                System.out.printf("Targeting %s() at line %d, col %d (Nth from left, post-filter)%n",
                        targetMethod,
                        targetCallExpr.getRange().get().begin.line,
                        targetCallExpr.getRange().get().begin.column);
            } else {
                System.err.printf("No matching method call found for %s at line %d (Nth=%d after filtering)%n",
                        targetMethod, targetLine, targetNth);
            }

            return super.visit(cu, arg);
        }



        @Override
        public Visitable visit(MethodCallExpr mce, Void arg) {
            mce = (MethodCallExpr) super.visit(mce, arg);
            if (targetCallExpr == null) return mce;

            if (mce.getRange().isPresent()
                    && targetCallExpr.getRange().isPresent()
                    && mce.getRange().get().equals(targetCallExpr.getRange().get())) {
                return splitOutCall(mce);
            }

            return mce;
        }


        /** Replace target call with a temp local variable */
        private NameExpr splitOutCall(MethodCallExpr call) {

            // Find enclosing method + body
            MethodDeclaration methodDecl =
                    call.findAncestor(MethodDeclaration.class)
                            .orElseThrow(() -> new IllegalStateException("No enclosing method found for call"));

            BlockStmt body = methodDecl.getBody()
                    .orElseThrow(() -> new IllegalStateException("Method has no body"));

            // --- Step 1: Generate a fresh unique variable name ---
            String varName = getFreshVarName(body);

            // --- Step 2: Type for variable (providedType or fallback) ---
            String typeStr = (providedType != null && !providedType.isEmpty())
                    ? providedType
                    : "var";

            Type parsedType = StaticJavaParser.parseType(typeStr);

            // --- Step 3: Add declaration at top of method ---
            VariableDeclarator vd = new VariableDeclarator(parsedType, varName);
            VariableDeclarator vd_to_be_asserted = null;
            VariableDeclarator vd_to_be_asserted_counter = null;
            if (isInLoop) {
                vd_to_be_asserted =  new VariableDeclarator(parsedType, "_assert_var_");
                vd_to_be_asserted_counter =  new VariableDeclarator(PrimitiveType.intType(), "_assert_counter_");
                vd_to_be_asserted_counter.setInitializer(new IntegerLiteralExpr(0));
            }

            if (parsedType.isPrimitiveType()) {
                PrimitiveType pt = parsedType.asPrimitiveType();
                switch (pt.getType()) {
                    case BOOLEAN: {
                        vd.setInitializer(new BooleanLiteralExpr(false));
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new BooleanLiteralExpr(false));
                        }
                        break;
                    }
                    case CHAR:
                    {
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new CharLiteralExpr('\u0000'));
                        }
                        vd.setInitializer(new CharLiteralExpr('\u0000'));

                        break;
                    }
                    case BYTE:
                    case SHORT:
                    case INT:     {
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new IntegerLiteralExpr(0));
                        }
                        vd.setInitializer(new IntegerLiteralExpr(0));
                        break;
                    }
                    case LONG:
                    {
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new LongLiteralExpr(0));
                        }
                        vd.setInitializer(new LongLiteralExpr(0));
                        break;
                    }
                    case FLOAT:
                    {
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new DoubleLiteralExpr(0.0f));
                        }
                        vd.setInitializer(new DoubleLiteralExpr(0.0f));
                        break;
                    }
                    case DOUBLE:
                    {
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new DoubleLiteralExpr(0.0));
                        }
                        vd.setInitializer(new DoubleLiteralExpr(0.0));
                        break;
                    }
                }
            } else {
                vd.setInitializer(new NullLiteralExpr());
                if (isInLoop) {
                    vd_to_be_asserted.setInitializer(new NullLiteralExpr());
                }
            }
            body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd)));
            if (isInLoop) {
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd_to_be_asserted)));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd_to_be_asserted_counter)));
            }



            // --- Step 4: Insert assignment before parent statement ---
            AssignExpr assignment = new AssignExpr(
                    new NameExpr(varName),
                    call.clone(),
                    AssignExpr.Operator.ASSIGN);

            Statement parentStmt = call.findAncestor(Statement.class)
                    .orElseThrow(() -> new IllegalStateException("No parent statement found"));
            BlockStmt parentBlock = parentStmt.findAncestor(BlockStmt.class)
                    .orElseThrow(() -> new IllegalStateException("No parent block found"));
            int idx = parentBlock.getStatements().indexOf(parentStmt);
            parentBlock.addStatement(idx, new ExpressionStmt(assignment));

            if (isInLoop) {
                // --- Step X: increment _assert_counter_ ---
                ExpressionStmt incrementCounter = new ExpressionStmt(
                        new UnaryExpr(new NameExpr("_assert_counter_"), UnaryExpr.Operator.POSTFIX_INCREMENT)
                );

// --- Step Y: if (_assert_counter_ == num) _assert_var_ = <varName>; ---
                BinaryExpr condition = new BinaryExpr(
                        new NameExpr("_assert_counter_"),
                        new IntegerLiteralExpr(loop),
                        BinaryExpr.Operator.EQUALS
                );

                AssignExpr assignExpr = new AssignExpr(
                        new NameExpr("_assert_var_"),
                        new NameExpr(varName),
                        AssignExpr.Operator.ASSIGN
                );

                IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(assignExpr), null);

// --- Step Z: insert them after your assignment ---
                parentBlock.addStatement(idx + 1, incrementCounter);
                parentBlock.addStatement(idx + 2, ifStmt);
            }

            // --- Step 5a: Insert verify call before every return ---
            for (ReturnStmt ret : body.findAll(ReturnStmt.class)) {
                if (ret.getExpression().isPresent()) {
                    // return; (void) — skip or handle separately
                    continue;
                }

                boolean checkException = false;
                Optional<MethodDeclaration> maybeMethod = ret.findAncestor(MethodDeclaration.class);
                if (maybeMethod.isPresent()) {
                    MethodDeclaration method = maybeMethod.get();
                    if (method.isAnnotationPresent("Test")) {
                        // Loop over all annotations
                        System.err.println("Annotation found!!");
                        for (AnnotationExpr ann : method.getAnnotations()) {
                            if (ann.getNameAsString().equals("expected")) {
                                checkException = true;
                            }
                        }
                    }
                } else {
                    System.out.printf("Return at line %d is not inside any method%n",
                            ret.getRange().map(r -> r.begin.line).orElse(-1));
                }
                if (checkException) {
                    //TODO
                    System.err.println("Early termination for exception");
                    // early termination
                    return new NameExpr(varName);
                }

                BlockStmt pBlock = ret.findAncestor(BlockStmt.class)
                        .orElseThrow(() -> new IllegalStateException("Return not inside a block"));
                int idx_1 = pBlock.getStatements().indexOf(ret);
                pBlock.addStatement(idx_1, makeVerifyStmt(READABLE_ACCESS, varName));
            }



             // --- Step 5b: Also add verify at end of method (if not already added) ---

            String varName_temp;
            if (isInLoop) {
                varName_temp = "_assert_var_";
            } else {
                varName_temp = varName;
            }
            boolean verifyExists = body.getStatements().stream()
                    .filter(Statement::isExpressionStmt)
                    .map(s -> s.asExpressionStmt().getExpression())
                    .filter(Expression::isMethodCallExpr)
                    .map(Expression::asMethodCallExpr)
                    .anyMatch(mc -> mc.getNameAsString().equals("verify")
                            && mc.getArguments().size() == 2
                            && mc.getArgument(1).toString().equals(varName_temp));

            if (!verifyExists) {
                if (isCheckingException(body)) {
                    String line_no = FixedStateUtils.getLastLineSuffix(new File(fixedStateFile));
                    // heuristics: add this earlier than exception, but later than the assignment statemetn.
                    insertBeforeLine(body, Integer.parseInt(line_no), makeVerifyStmt(READABLE_ACCESS, varName_temp),idx,isInLoop);
                } else {
                    body.addStatement(makeVerifyStmt(READABLE_ACCESS, varName_temp));
                }

            }


            // --- Step 6: Avoid dangling "__ins_vX;" ---
            if (parentStmt.isExpressionStmt() &&
                    parentStmt.asExpressionStmt().getExpression().equals(call)) {
                // Do not replace; just remove the call (effectively, nothing to return here)
                return null; // keeps the statement untouched, since assignment already added
            }

// Otherwise replace the expression with the variable reference
            return new NameExpr(varName);

        }



    }

    public static void insertBeforeLine(BlockStmt body, int lineNumber, Statement newStmt, int index, boolean isInLoop) {
        List<Statement> stmts = body.getStatements();
        int insertIndex = stmts.size(); // default to end of block
        //index is the index of the variable assignment statement
        int closestLine = Integer.MIN_VALUE;

        for (int i = 0; i < stmts.size(); i++) {
            Optional<Range> range = stmts.get(i).getRange();
            if (range.isPresent()) {
                int stmtLine = range.get().begin.line;
                // Find the statement with the largest line < target
                if (stmtLine < lineNumber && stmtLine > closestLine) {
                    closestLine = stmtLine;
                    insertIndex = i + 1; // insert right *after* this statement
                }
            }
        }
//        5 2
            // index is the where the assignment is inserted
            // insertIndex is where the last executed statements.


        System.err.println("haha: " + insertIndex + " - "+ index);
        if (lineNumber != TARGET_LINE) {
            //

            //        if (insertIndex == index) {
            if (isInLoop) {
                insertIndex += 3;

                if (insertIndex >= stmts.size() - 1) {
                    System.err.println("insertIndex too much: ");
                }
                System.err.println(":" + insertIndex + " - "+ (stmts.size() - 1));
                insertIndex = Math.min(insertIndex, stmts.size() -1);

            } else {
                insertIndex += 1;
                insertIndex = Math.min(insertIndex, stmts.size() -1);

            }
        } else {

            System.err.println(":::" + insertIndex + " - "+ (stmts.size() - 1));
            if (insertIndex  < stmts.size() - 1) {
                insertIndex += 1;
            }
            insertIndex = Math.min(insertIndex, stmts.size() -1);
        }

        body.addStatement(insertIndex, newStmt);

        System.out.printf("✅ Inserted verify before closest-to-line %d (insert index=%d)%n",
                lineNumber, insertIndex);
    }

    public static boolean isCheckingException(BlockStmt body) {
        // Find the enclosing method of this block
        Optional<MethodDeclaration> maybeMethod = body.findAncestor(MethodDeclaration.class);
        if (!maybeMethod.isPresent()) {
            return false;
        }

        MethodDeclaration method = maybeMethod.get();

        // Look for @Test annotations (JUnit4 style)
        for (AnnotationExpr ann : method.getAnnotations()) {
            if (ann.getNameAsString().equals("Test")) {
                // NormalAnnotationExpr -> @Test(key = value, ...)
                if (ann.isNormalAnnotationExpr()) {
                    NormalAnnotationExpr nae = ann.asNormalAnnotationExpr();
                    for (MemberValuePair pair : nae.getPairs()) {
                        // Look for the "expected" attribute
                        if (pair.getNameAsString().equals("expected")) {
                            System.out.printf("✅ Method %s has @Test(expected=%s)%n",
                                    method.getNameAsString(), pair.getValue());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }




    static class ConstructorCaptureRewriter extends ModifierVisitor<Void> {
        private String targetType;
        private final int targetLine;
        private final int targetNth;
        private final String providedType;
        private final AtomicInteger fresh = new AtomicInteger(1);
        private ObjectCreationExpr targetCtorExpr;

        ConstructorCaptureRewriter(String targetType, int targetLine, int targetNth, String providedType) {
            this.targetType = targetType;
            this.targetLine = targetLine;
            this.targetNth = targetNth;
            this.providedType = providedType;
        }

        @Override
        public Visitable visit(CompilationUnit cu, Void arg) {
            String[] temp = targetType.split("\\$");
            if (temp.length > 1) {
                targetType = temp[temp.length - 1];
            }

            final int maxDownwardWindow = 50; // maximum number of lines to look ahead
            List<ObjectCreationExpr> candidates = new ArrayList<>();


            // search starting from targetLine, going downward line by line
            for (int offset = 0; offset <= maxDownwardWindow && candidates.isEmpty(); offset++) {
                int currentLine = targetLine + offset;

                candidates = cu.findAll(ObjectCreationExpr.class).stream()
                        .filter(c -> c.getType().getNameAsString().equals(targetType))
                        .filter(c -> c.getRange().map(r ->
                                currentLine >= r.begin.line && currentLine <= r.end.line
                        ).orElse(false))
                        .sorted(Comparator.comparingInt(c -> c.getRange().get().begin.column))
                        .collect(Collectors.toList());

                if (!candidates.isEmpty() && offset > 0) {
                    System.out.printf("⚠️ Fallback: found constructor %s() %d line(s) below target (at %d)%n",
                            targetType, offset, currentLine);
                }
            }

            if (targetNth >= 0 && targetNth < candidates.size()) {
                targetCtorExpr = candidates.get(targetNth);
                System.out.printf("✅ Targeting new %s() at line %d, col %d%n",
                        targetType,
                        targetCtorExpr.getRange().get().begin.line,
                        targetCtorExpr.getRange().get().begin.column);
            } else {
                System.out.printf("❌ No matching constructor found for %s within +%d lines of %d (Nth=%d)%n",
                        targetType, maxDownwardWindow, targetLine, targetNth);
            }

            return super.visit(cu, arg);
        }



        @Override
        public Visitable visit(ObjectCreationExpr oce, Void arg) {
            oce = (ObjectCreationExpr) super.visit(oce, arg);
            if (targetCtorExpr == null) return oce;

            if (oce.getRange().isPresent()
                    && targetCtorExpr.getRange().isPresent()
                    && oce.getRange().get().equals(targetCtorExpr.getRange().get())) {
                return splitOutCtor(oce);
            }

            return oce;
        }



        private NameExpr splitOutCtor(ObjectCreationExpr ctor) {
            // Find enclosing method + body
            MethodDeclaration methodDecl = ctor.findAncestor(MethodDeclaration.class)
                    .orElseThrow(() -> new IllegalStateException("No enclosing method found for constructor call"));
            BlockStmt body = methodDecl.getBody()
                    .orElseThrow(() -> new IllegalStateException("Method has no body"));

            // --- Step 1: Generate a fresh unique variable name ---
            String varName = getFreshVarName(body);


            // --- Step 2: Type for variable (providedType or fallback) ---
//            String typeStr = (providedType != null && !providedType.isEmpty())
//                    ? providedType
//                    : ctor.getType().asString(); // fallback: constructor type
            String typeStr = ctor.getType().getNameAsString();

// --- Step 3: Add declaration at top, initialized to null ---
            VariableDeclarator vd = new VariableDeclarator(
                    StaticJavaParser.parseType(typeStr), varName, new NullLiteralExpr());
            // --- Step 3: Add declaration at top of method ---
            VariableDeclarator vd_to_be_asserted = null;
            VariableDeclarator vd_to_be_asserted_counter = null;
            if (isInLoop) {
                vd_to_be_asserted =  new VariableDeclarator(StaticJavaParser.parseType(typeStr), "_assert_var_", new NullLiteralExpr());
                vd_to_be_asserted_counter =  new VariableDeclarator(PrimitiveType.intType(), "_assert_counter_");
                vd_to_be_asserted_counter.setInitializer(new IntegerLiteralExpr(0));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd_to_be_asserted)));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd_to_be_asserted_counter)));
            }


            body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd)));


            Statement parentStmt = ctor.findAncestor(Statement.class)
                    .orElseThrow(() -> new IllegalStateException("No parent statement found for constructor call"));
            BlockStmt parentBlock = parentStmt.findAncestor(BlockStmt.class)
                    .orElseThrow(() -> new IllegalStateException("No parent block found for statement"));
            int idx = parentBlock.getStatements().indexOf(parentStmt);

            AssignExpr assignment = new AssignExpr(
                    new NameExpr(varName),
                    ctor.clone(),
                    AssignExpr.Operator.ASSIGN);
            parentBlock.addStatement(idx, new ExpressionStmt(assignment));


            if (isInLoop) {
                // --- Step X: increment _assert_counter_ ---
                ExpressionStmt incrementCounter = new ExpressionStmt(
                        new UnaryExpr(new NameExpr("_assert_counter_"), UnaryExpr.Operator.POSTFIX_INCREMENT)
                );

                BinaryExpr condition = new BinaryExpr(
                        new NameExpr("_assert_counter_"),
                        new IntegerLiteralExpr(loop),
                        BinaryExpr.Operator.EQUALS
                );

                AssignExpr assignExpr = new AssignExpr(
                        new NameExpr("_assert_var_"),
                        new NameExpr(varName),
                        AssignExpr.Operator.ASSIGN
                );

                IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(assignExpr), null);

                parentBlock.addStatement(idx + 1, incrementCounter);
                parentBlock.addStatement(idx + 2, ifStmt);
            }


            // --- Step 5a: Insert verify call before every return ---
            for (ReturnStmt ret : body.findAll(ReturnStmt.class)) {
                if (ret.getExpression().isPresent()) {
                    // return; (void) — skip or handle separately
                    continue;
                }
                BlockStmt pBlock = ret.findAncestor(BlockStmt.class)
                        .orElseThrow(() -> new IllegalStateException("Return not inside a block"));
                int idx_1 = pBlock.getStatements().indexOf(ret);
                pBlock.addStatement(idx_1, makeVerifyStmt(READABLE_ACCESS, varName));
            }

            String varName_temp;
            if (isInLoop) {
                varName_temp = "_assert_var_";
            } else {
                varName_temp = varName;
            }

            boolean verifyExists = body.getStatements().stream()
                    .filter(Statement::isExpressionStmt)
                    .map(s -> s.asExpressionStmt().getExpression())
                    .filter(Expression::isMethodCallExpr)
                    .map(Expression::asMethodCallExpr)
                    .anyMatch(mc -> mc.getNameAsString().equals("verify")
                            && mc.getArguments().size() == 2
                            && mc.getArgument(1).toString().equals(varName_temp));



            if (!verifyExists) {
                if (isCheckingException(body)) {
                    String line_no = FixedStateUtils.getLastLineSuffix(new File(fixedStateFile));
                    // heuristics: add this earlier than exception, but later than the assignment statemetn.
                    insertBeforeLine(body, Integer.parseInt(line_no), makeVerifyStmt(READABLE_ACCESS, varName_temp), idx, isInLoop);
                } else {
                    body.addStatement(makeVerifyStmt(READABLE_ACCESS, varName_temp));
                }
            }

            if (parentStmt.isExpressionStmt() &&
                    parentStmt.asExpressionStmt().getExpression().equals(ctor)) {
                // Already handled by inserted assignment
                return null; // don’t replace with varName
            }

            return new NameExpr(varName);

        }

    }

    /** Utility: ensures fresh unique variable name */
    private static String getFreshVarName(BlockStmt body) {
        for (;;) {
            String candidate = "__ins_v" + fresh.getAndIncrement();

            boolean exists = false;
            for (VariableDeclarator v : body.findAll(VariableDeclarator.class)) {
                if (v.getNameAsString().equals(candidate)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) return candidate;
        }
    }

    /** Capture field dereferences (getField) and route through a temp local + verify */
    static class FieldCaptureRewriter extends ModifierVisitor<Void> {
        private final String fieldName;
        private final int targetLine;
        private final int targetNth;
        private final String providedType;

        private Expression targetFieldExpr = null; // May be FieldAccessExpr or NameExpr
        private final AtomicInteger fresh = new AtomicInteger(1);

        FieldCaptureRewriter(String fieldName, int targetLine, int targetNth, String providedType) {
            this.fieldName = fieldName;
            this.targetLine = targetLine;
            this.targetNth = targetNth;
            this.providedType = providedType;
        }

        @Override
        public Visitable visit(CompilationUnit cu, Void arg) {
            final int maxDownwardWindow = 50; // how many lines below to search
            targetFieldExpr = null;

            // Helper to find candidates on a given line
            BiFunction<Integer, Boolean, List<Expression>> findCandidatesOnLine = (line, includeFieldAccess) -> {
                List<Expression> candidates = new ArrayList<>();
                if (includeFieldAccess) {
                    candidates.addAll(
                            cu.findAll(FieldAccessExpr.class).stream()
                                    .filter(fae -> fae.getNameAsString().equals(fieldName))
                                    .filter(fae -> fae.getRange().map(r -> line >= r.begin.line && line <= r.end.line).orElse(false))
                                    .sorted(Comparator.comparingInt(fae -> fae.getRange().map(r -> r.begin.column).orElse(Integer.MAX_VALUE)))
                                    .collect(Collectors.toList())
                    );
                    Collections.reverse(candidates); // handle nested field accesses right-to-left
                } else {
                    candidates.addAll(
                            cu.findAll(NameExpr.class).stream()
                                    .filter(ne -> ne.getNameAsString().equals(fieldName))
                                    .filter(ne -> ne.getRange().map(r -> line >= r.begin.line && line <= r.end.line).orElse(false))
                                    .sorted(Comparator.comparingInt(ne -> ne.getRange().map(r -> r.begin.column).orElse(Integer.MAX_VALUE)))
                                    .collect(Collectors.toList())
                    );
                    Collections.reverse(candidates);
                }
                return candidates;
            };

            // Step 1: Try exact target line first
            List<Expression> candidates = findCandidatesOnLine.apply(targetLine, true);
            if (candidates.isEmpty()) {
                candidates = findCandidatesOnLine.apply(targetLine, false);
            }

            // Step 2: Expand downward if not found
            for (int offset = 1; offset <= maxDownwardWindow && candidates.isEmpty(); offset++) {
                int currentLine = targetLine + offset;

                candidates = findCandidatesOnLine.apply(currentLine, true);
                if (candidates.isEmpty()) {
                    candidates = findCandidatesOnLine.apply(currentLine, false);
                }

                if (!candidates.isEmpty()) {
                    System.out.printf("⚠️ Fallback: found field '%s' %d line(s) below (at line %d)%n",
                            fieldName, offset, currentLine);
                    break;
                }
            }

            // Step 3: Pick Nth candidate if available
            if (targetNth >= 0 && targetNth < candidates.size()) {
                targetFieldExpr = candidates.get(targetNth);
                System.out.printf("✅ Targeting field '%s' at line %d, col %d (Nth=%d)%n",
                        fieldName,
                        targetFieldExpr.getRange().get().begin.line,
                        targetFieldExpr.getRange().get().begin.column,
                        targetNth);
            } else {
                System.err.printf("❌ No matching field access found for '%s' within +%d lines of line %d (Nth=%d)%n",
                        fieldName, maxDownwardWindow, targetLine, targetNth);
            }

            return super.visit(cu, arg);
        }




        @Override
        public Visitable visit(FieldAccessExpr fae, Void arg) {
            fae = (FieldAccessExpr) super.visit(fae, arg);
            if (targetFieldExpr == null || !fae.getRange().isPresent() || !targetFieldExpr.getRange().isPresent()) return fae;
            if (fae.getRange().get().equals(targetFieldExpr.getRange().get())) {
                return splitOutField(fae);
            }
            return fae;
        }

        @Override
        public Visitable visit(NameExpr ne, Void arg) {
            ne = (NameExpr) super.visit(ne, arg);
            if (targetFieldExpr == null || !ne.getRange().isPresent() || !targetFieldExpr.getRange().isPresent()) return ne;
            if (ne.getRange().get().equals(targetFieldExpr.getRange().get())) {
                return splitOutField(ne);
            }
            return ne;
        }

        /** Replace target field expression with a temp local variable (with assignment before parent stmt) */
        private NameExpr splitOutField(Expression fieldExpr) {
            MethodDeclaration methodDecl =
                    fieldExpr.findAncestor(MethodDeclaration.class)
                            .orElseThrow(() -> new IllegalStateException("No enclosing method found for field access"));
            BlockStmt body = methodDecl.getBody()
                    .orElseThrow(() -> new IllegalStateException("Method has no body"));

            // --- Step 1: fresh temp name
            String varName = getFreshVarName(body);

            // --- Step 2: declare at top with initializer (primitive defaults / null)
            String typeStr = (providedType != null && !providedType.isEmpty()) ? providedType : "var";
            Type parsedType = StaticJavaParser.parseType(typeStr);

            VariableDeclarator vd = new VariableDeclarator(parsedType, varName);
            VariableDeclarator vd_to_be_asserted = null;
            VariableDeclarator vd_to_be_asserted_counter = null;
            if (isInLoop) {
                vd_to_be_asserted =  new VariableDeclarator(parsedType, "_assert_var_");
                vd_to_be_asserted_counter =  new VariableDeclarator(PrimitiveType.intType(), "_assert_counter_");
                vd_to_be_asserted_counter.setInitializer(new IntegerLiteralExpr(0));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd_to_be_asserted)));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd_to_be_asserted_counter)));
            }

            if (parsedType.isPrimitiveType()) {
                PrimitiveType pt = parsedType.asPrimitiveType();
                switch (pt.getType()) {
                    case BOOLEAN: {
                        vd.setInitializer(new BooleanLiteralExpr(false));
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new BooleanLiteralExpr(false));
                        }
                        break;
                    }
                    case CHAR: {
                        vd.setInitializer(new CharLiteralExpr('\u0000'));
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new CharLiteralExpr('\u0000'));
                        }
                        break;
                    }
                    case BYTE:
                    case SHORT:
                    case INT: {
                        vd.setInitializer(new IntegerLiteralExpr(0));
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new IntegerLiteralExpr(0));
                        }
                        break;
                    }
                    case LONG: {
                        vd.setInitializer(new LongLiteralExpr(0));
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new LongLiteralExpr(0));
                        }
                        break;
                    }
                    case FLOAT:   {
                        vd.setInitializer(new DoubleLiteralExpr(0.0f));
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new DoubleLiteralExpr(0.0f));
                        }
                        break;
                    }
                    case DOUBLE:
                    {
                        vd.setInitializer(new DoubleLiteralExpr(0.0));
                        if (isInLoop) {
                            vd_to_be_asserted.setInitializer(new DoubleLiteralExpr(0.0));
                        }
                        break;
                    }
                }
            } else {
                vd.setInitializer(new NullLiteralExpr());
                if (isInLoop) {
                    vd_to_be_asserted.setInitializer(new NullLiteralExpr());
                }
            }
            body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(vd)));

            // --- Step 3: insert assignment before parent statement
            AssignExpr assignment = new AssignExpr(new NameExpr(varName), fieldExpr.clone(), AssignExpr.Operator.ASSIGN);
            Statement parentStmt = fieldExpr.findAncestor(Statement.class)
                    .orElseThrow(() -> new IllegalStateException("No parent statement found for field access"));
            BlockStmt parentBlock = parentStmt.findAncestor(BlockStmt.class)
                    .orElseThrow(() -> new IllegalStateException("No parent block found"));
            int idx = parentBlock.getStatements().indexOf(parentStmt);
            parentBlock.addStatement(idx, new ExpressionStmt(assignment));

            if (isInLoop) {
                // --- Step X: increment _assert_counter_ ---
                ExpressionStmt incrementCounter = new ExpressionStmt(
                        new UnaryExpr(new NameExpr("_assert_counter_"), UnaryExpr.Operator.POSTFIX_INCREMENT)
                );

// --- Step Y: if (_assert_counter_ == num) _assert_var_ = <varName>; ---
                BinaryExpr condition = new BinaryExpr(
                        new NameExpr("_assert_counter_"),
                        new IntegerLiteralExpr(loop),
                        BinaryExpr.Operator.EQUALS
                );

                AssignExpr assignExpr = new AssignExpr(
                        new NameExpr("_assert_var_"),
                        new NameExpr(varName),
                        AssignExpr.Operator.ASSIGN
                );

                IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(assignExpr), null);

// --- Step Z: insert them after your assignment ---
                parentBlock.addStatement(idx + 1, incrementCounter);
                parentBlock.addStatement(idx + 2, ifStmt);
            }

            // --- Step 4a: verify before every return
            String varName_temp;
            if (isInLoop) {
                varName_temp = "_assert_var_";
            } else {
                varName_temp = varName;
            }
            for (ReturnStmt ret : body.findAll(ReturnStmt.class)) {
                if (ret.getExpression().isPresent()) {
                    continue;
                }
                BlockStmt pBlock = ret.findAncestor(BlockStmt.class)
                        .orElseThrow(() -> new IllegalStateException("Return not inside a block"));
                int ridx = pBlock.getStatements().indexOf(ret);
                pBlock.addStatement(ridx, makeVerifyStmt(READABLE_ACCESS, varName_temp));
            }

            // --- Step 4b: verify at end if not already
            boolean verifyExists = body.getStatements().stream()
                    .filter(Statement::isExpressionStmt)
                    .map(s -> s.asExpressionStmt().getExpression())
                    .filter(Expression::isMethodCallExpr)
                    .map(Expression::asMethodCallExpr)
                    .anyMatch(mc -> mc.getNameAsString().equals("verify")
                            && mc.getArguments().size() == 2
                            && mc.getArgument(1).toString().equals(varName_temp));
            if (!verifyExists) {
                if (isCheckingException(body)) {
                    String line_no = FixedStateUtils.getLastLineSuffix(new File(fixedStateFile));
                    // heuristics: add this earlier than exception, but later than the assignment statemetn.
                    insertBeforeLine(body, Integer.parseInt(line_no), makeVerifyStmt(READABLE_ACCESS, varName_temp),idx,isInLoop);
                } else {
                    body.addStatement(makeVerifyStmt(READABLE_ACCESS, varName_temp));
                }
            }

            // --- Step 5: avoid dangling "__ins_vX;"
            if (parentStmt.isExpressionStmt() &&
                    parentStmt.asExpressionStmt().getExpression().equals(fieldExpr)) {
                return null; // leave original statement as-is; assignment already added
            }

            // Otherwise replace the field expression where it appears inside a larger expression
            return new NameExpr(varName);
        }
    }


    private static ExpressionStmt makeVerifyStmt(String readableAccess, String varName) {
        MethodCallExpr verifyCall = new MethodCallExpr(
                new NameExpr("org.helper.Assertions"), "verify");
        verifyCall.addArgument(new StringLiteralExpr(readableAccess));
        verifyCall.addArgument(new NameExpr(varName));
        return new ExpressionStmt(verifyCall);
    }




    private static String descriptorToJavaType(String desc) {
        switch (desc) {
            case "I": return "int";
            case "Z": return "boolean";
            case "B": return "byte";
            case "S": return "short";
            case "C": return "char";
            case "J": return "long";
            case "F": return "float";
            case "D": return "double";
        }
        if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1).replace('/', '.');
        }
        if (desc.startsWith("[L") && desc.endsWith(";")) {
            String inner = desc.substring(2, desc.length() - 1).replace('/', '.');
            return inner + "[]";
        }
        if (desc.startsWith("[")) { // primitive arrays
            return descriptorToJavaType(desc.substring(1)) + "[]";
        }
        throw new IllegalArgumentException("Unsupported descriptor: " + desc);
    }

    public static class LocalCaptureRewriter extends ModifierVisitor<Void> {

        private final String localName;
        private final int targetLine;
        private final int targetNth;
        private final String typeDescriptor;

        private VariableDeclarator targetDecl = null;
        private AssignExpr targetAssign = null;
        private Expression targetExpr = null;
        private final AtomicInteger fresh = new AtomicInteger(1);

        private boolean instrumented = false;

        public LocalCaptureRewriter(String localName, int targetLine, int targetNth, String typeDescriptor) {
            this.localName = localName;
            this.targetLine = targetLine;
            this.targetNth = targetNth;
            this.typeDescriptor = typeDescriptor;
        }

        @Override
        public Visitable visit(CompilationUnit cu, Void arg) {
            // ✅ Step 0: explicitly prioritize catch clause first
            cu.findAll(CatchClause.class).forEach(cc -> {
                if (!instrumented) checkAndInstrumentCatch(cc);
            });

            if (instrumented) {
                // Skip rest of traversal entirely — catch already handled
                return cu;
            }

            // Normal matching logic follows
            String expectedType = descriptorToJavaType(this.typeDescriptor);
            final int maxUpwardWindow = 50;

            // Step 1: VariableDeclarator
            List<VariableDeclarator> decls = cu.findAll(VariableDeclarator.class).stream()
                    .filter(vd -> vd.getNameAsString().equals(localName))
                    .filter(vd -> {
                        String declaredType = baseTypeName(vd.getType());
                        return declaredType.equals(expectedType)
                                || expectedType.endsWith("." + declaredType);
                    })
                    .filter(vd -> vd.getRange().isPresent()
                            && vd.getRange().get().begin.line <= targetLine
                            && vd.getRange().get().end.line >= targetLine)
                    .collect(Collectors.toList());
            if (!decls.isEmpty() && targetNth < decls.size()) {
                targetDecl = decls.get(targetNth);
                actual_targetLine = targetDecl.getRange().get().begin.line;
                System.out.printf("✅ Targeting declaration '%s' at line %d%n",
                        localName, targetDecl.getRange().get().begin.line);
                return super.visit(cu, arg);
            }

            // Step 2: AssignExpr
            List<AssignExpr> assigns = cu.findAll(AssignExpr.class).stream()
                    .filter(a -> a.getTarget().isNameExpr()
                            && a.getTarget().asNameExpr().getNameAsString().equals(localName))
                    .filter(a -> a.getRange().isPresent()
                            && a.getRange().get().begin.line <= targetLine
                            && a.getRange().get().end.line >= targetLine)
                    .collect(Collectors.toList());
            if (!assigns.isEmpty() && targetNth < assigns.size()) {
                targetAssign = assigns.get(targetNth);
                System.out.printf("✅ Targeting assignment '%s' at line %d%n",
                        localName, targetAssign.getRange().get().begin.line);
                actual_targetLine = targetAssign.getRange().get().begin.line;
                return super.visit(cu, arg);
            }

            // Step 3: NameExpr
            List<NameExpr> names = cu.findAll(NameExpr.class).stream()
                    .filter(ne -> ne.getNameAsString().equals(localName))
                    .filter(ne -> ne.getRange().isPresent()
                            && ne.getRange().get().begin.line <= targetLine
                            && ne.getRange().get().end.line >= targetLine)
                    .collect(Collectors.toList());
            if (!names.isEmpty() && targetNth < names.size()) {
                targetExpr = names.get(targetNth);
                System.out.printf("✅ Targeting usage '%s' at line %d%n",
                        localName, targetExpr.getRange().get().begin.line);
                actual_targetLine = targetExpr.getRange().get().begin.line;
                return super.visit(cu, arg);
            }

            // Step 4: FieldAccessExpr
            List<FieldAccessExpr> fields = cu.findAll(FieldAccessExpr.class).stream()
                    .filter(fae -> fae.getNameAsString().equals(localName))
                    .filter(fae -> fae.getRange().isPresent()
                            && fae.getRange().get().begin.line <= targetLine
                            && fae.getRange().get().end.line >= targetLine)
                    .collect(Collectors.toList());
            if (!fields.isEmpty() && targetNth < fields.size()) {
                targetExpr = fields.get(targetNth);
                System.out.printf("✅ Targeting field-like usage '%s' at line %d%n",
                        localName, targetExpr.getRange().get().begin.line);
                actual_targetLine = targetExpr.getRange().get().begin.line;
                return super.visit(cu, arg);
            }

            // Step 5: Upward search window
            for (int offset = 1; offset <= maxUpwardWindow && !instrumented; offset++) {
                int checkLine = targetLine - offset;
                if (checkLine <= 0) break;


                List<VariableDeclarator> declsUp = cu.findAll(VariableDeclarator.class).stream()
                        .filter(vd -> vd.getNameAsString().equals(localName))
                        .filter(vd -> {
                            String declaredType = baseTypeName(vd.getType());
                            return declaredType.equals(expectedType)
                                    || expectedType.endsWith("." + declaredType);
                        })
                        .filter(vd -> vd.getRange().isPresent()
                                && vd.getRange().get().end.line >= checkLine
                                && vd.getRange().get().begin.line <= checkLine)
                        .collect(Collectors.toList());
                if (!declsUp.isEmpty()) {
                    targetDecl = declsUp.get(0);
                    actual_targetLine = targetDecl.getRange().get().begin.line;
                    System.out.printf("⚠️ Fallback: found declaration '%s' %d lines above at line %d%n",
                            localName, offset, targetDecl.getRange().get().begin.line);
                    break;
                }

                List<AssignExpr> assignsUp = cu.findAll(AssignExpr.class).stream()
                        .filter(a -> a.getTarget().isNameExpr()
                                && a.getTarget().asNameExpr().getNameAsString().equals(localName))
                        .filter(a -> a.getRange().isPresent()
                                && a.getRange().get().end.line >= checkLine
                                && a.getRange().get().begin.line <= checkLine)
                        .collect(Collectors.toList());
                if (!assignsUp.isEmpty()) {
                    targetAssign = assignsUp.get(0);
                    actual_targetLine = targetAssign.getRange().get().begin.line;
                    System.out.printf("⚠️ Fallback: found assignment '%s' %d lines above at line %d%n",
                            localName, offset, targetAssign.getRange().get().begin.line);
                    break;
                }

                List<NameExpr> namesUp = cu.findAll(NameExpr.class).stream()
                        .filter(ne -> ne.getNameAsString().equals(localName))
                        .filter(ne -> ne.getRange().isPresent()
                                && ne.getRange().get().begin.line <= checkLine
                                && ne.getRange().get().end.line >= checkLine)
                        .collect(Collectors.toList());
                if (!namesUp.isEmpty()) {
                    targetExpr = namesUp.get(0);
                    actual_targetLine = targetExpr.getRange().get().begin.line;
                    System.out.printf("⚠️ Fallback: found usage '%s' %d lines above at line %d%n",
                            localName, offset, targetExpr.getRange().get().begin.line);
                    break;
                }

                List<FieldAccessExpr> fieldsUp = cu.findAll(FieldAccessExpr.class).stream()
                        .filter(fae -> fae.getNameAsString().equals(localName))
                        .filter(fae -> fae.getRange().isPresent()
                                && fae.getRange().get().begin.line <= checkLine
                                && fae.getRange().get().end.line >= checkLine)
                        .collect(Collectors.toList());
                if (!fieldsUp.isEmpty()) {
                    targetExpr = fieldsUp.get(0);
                    actual_targetLine = targetExpr.getRange().get().begin.line;
                    System.out.printf("⚠️ Fallback: found field '%s' %d lines above at line %d%n",
                            localName, offset, targetExpr.getRange().get().begin.line);
                    break;
                }
            }

            return super.visit(cu, arg);
        }

        private void checkAndInstrumentCatch(CatchClause cc) {
            Parameter param = cc.getParameter();
            if (instrumented) return;

            if (param.getNameAsString().equals(localName)
                    && param.getRange().isPresent()
                    && param.getRange().get().begin.line <= targetLine
                    && param.getRange().get().end.line >= targetLine) {

                BlockStmt body = cc.getBody();
                body.addStatement(0, makeVerifyStmt(READABLE_ACCESS, param.getNameAsString()));
                System.out.printf("✅ [PRIORITY] Inserted verify for catch parameter '%s' at line %d%n",
                        localName, param.getRange().get().begin.line);
                instrumented = true;
            }
        }

        @Override
        public Visitable visit(VariableDeclarator vd, Void arg) {
            // Safely get line number if available
            AtomicInteger ln = new AtomicInteger();
            vd.getRange().ifPresent(range -> {
                int lineNumber = range.begin.line;
                ln.set(lineNumber);
            });

            vd = (VariableDeclarator) super.visit(vd, arg);
            if (!instrumented && targetDecl != null && vd.equals(targetDecl) && ln.get() == actual_targetLine) {
                instrumentDeclaration(vd);
                instrumented = true;
            }
            return vd;
        }

        @Override
        public Visitable visit(AssignExpr ae, Void arg) {
            ae = (AssignExpr) super.visit(ae, arg);
            AtomicInteger ln = new AtomicInteger();
            ae.getRange().ifPresent(range -> {
                int lineNumber = range.begin.line;
                ln.set(lineNumber);
            });
            if (!instrumented && targetAssign != null && ae.equals(targetAssign) && ln.get() == actual_targetLine) {
                instrumentAssignment(ae);
                instrumented = true;
            }
            return ae;
        }

        // ───────────────────────────────────────────────
// 🔧 Helper Methods
// ───────────────────────────────────────────────

        private Expression getDefaultValue(Type type) {
            if (type.isPrimitiveType()) {
                PrimitiveType pt = type.asPrimitiveType();
                switch (pt.getType()) {
                    case BOOLEAN:
                        return new BooleanLiteralExpr(false);
                    case CHAR:
                        return new CharLiteralExpr('\u0000'); // or just ''
                    case BYTE:
                    case SHORT:
                    case INT:
                    case LONG:
                        return new IntegerLiteralExpr(0);
                    case FLOAT:
                    case DOUBLE:
                        return new DoubleLiteralExpr(0.0);
                }
            }
            // For non-primitives
            return new NullLiteralExpr();
        }

        private void instrumentDeclaration(VariableDeclarator vd) {
            MethodDeclaration method = vd.findAncestor(MethodDeclaration.class)
                    .orElseThrow(() -> new IllegalStateException("No enclosing method for declaration"));

            BlockStmt body = method.getBody()
                    .orElseThrow(() -> new IllegalStateException("Method has no body"));

            String varName = getFreshVarName(body);
            Type parsedType = vd.getType();

            // tracker variable at top
            VariableDeclarator tracker = new VariableDeclarator(parsedType, varName, getDefaultValue(parsedType));
            body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(tracker)));
            if (isInLoop) {
                VariableDeclarator tracker_1 = new VariableDeclarator(parsedType, "_assert_var_", getDefaultValue(parsedType));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(tracker_1)));
                VariableDeclarator tracker_2 = new VariableDeclarator(PrimitiveType.intType(), "_assert_counter_", new IntegerLiteralExpr(0));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(tracker_2)));
            }

            // add tracker assignment after declaration
            Statement parentStmt = vd.findAncestor(Statement.class).get();
            BlockStmt parentBlock = parentStmt.findAncestor(BlockStmt.class).get();
            int idx = parentBlock.getStatements().indexOf(parentStmt);
            parentBlock.addStatement(idx + 1,
                    new ExpressionStmt(new AssignExpr(new NameExpr(varName),
                            vd.getNameAsExpression().clone(),
                            AssignExpr.Operator.ASSIGN)));

            // assign to _assert_var_ if in loop
            if (isInLoop) {
                // --- Step Y: if (_assert_counter_ == num) _assert_var_ = <varName>; ---
                BinaryExpr condition = new BinaryExpr(
                        new NameExpr("_assert_counter_"),
                        new IntegerLiteralExpr(loop),
                        BinaryExpr.Operator.EQUALS
                );

                AssignExpr assignExpr = new AssignExpr(
                        new NameExpr("_assert_var_"),
                        new NameExpr(varName),
                        AssignExpr.Operator.ASSIGN
                );

                //increment
                ExpressionStmt incrementCounter = new ExpressionStmt(
                        new UnaryExpr(new NameExpr("_assert_counter_"), UnaryExpr.Operator.POSTFIX_INCREMENT)
                );
                parentBlock.addStatement(idx + 1, incrementCounter);
                IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(assignExpr), null);
                parentBlock.addStatement(idx + 2, ifStmt);

            }

            String varName_temp;
            if (isInLoop) {
                varName_temp = "_assert_var_";
            } else {
                varName_temp = varName;
            }
            addVerify(body, varName_temp,idx,isInLoop);
        }

        private void instrumentAssignment(AssignExpr ae) {
            MethodDeclaration method = ae.findAncestor(MethodDeclaration.class)
                    .orElseThrow(() -> new IllegalStateException("No enclosing method for assignment"));

            BlockStmt body = method.getBody()
                    .orElseThrow(() -> new IllegalStateException("Method has no body"));

            String varName = getFreshVarName(body);
            Type parsedType = StaticJavaParser.parseType(descriptorToJavaType(this.typeDescriptor));

            VariableDeclarator tracker = new VariableDeclarator(parsedType, varName, new NullLiteralExpr());
            body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(tracker)));

            VariableDeclarator tracker_1 = null;
            VariableDeclarator tracker_2 = null;
            if (isInLoop) {
                tracker_1 = new VariableDeclarator(parsedType, "_assert_var_", new NullLiteralExpr());
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(tracker_1)));
                tracker_2 = new VariableDeclarator(PrimitiveType.intType(), "_assert_counter_", new IntegerLiteralExpr(0));
                body.addStatement(0, new ExpressionStmt(new VariableDeclarationExpr(tracker_2)));
            }

            Statement parentStmt = ae.findAncestor(Statement.class).get();
            BlockStmt parentBlock = parentStmt.findAncestor(BlockStmt.class).get();
            int idx = parentBlock.getStatements().indexOf(parentStmt);
            parentBlock.addStatement(idx + 1,
                    new ExpressionStmt(new AssignExpr(new NameExpr(varName),
                            ae.getTarget().clone(),
                            AssignExpr.Operator.ASSIGN)));

            if (isInLoop) {
                // --- Step Y: if (_assert_counter_ == num) _assert_var_ = <varName>; ---
                BinaryExpr condition = new BinaryExpr(
                        new NameExpr("_assert_counter_"),
                        new IntegerLiteralExpr(loop),
                        BinaryExpr.Operator.EQUALS
                );

                AssignExpr assignExpr = new AssignExpr(
                        new NameExpr("_assert_var_"),
                        new NameExpr(varName),
                        AssignExpr.Operator.ASSIGN
                );

                //increment
                ExpressionStmt incrementCounter = new ExpressionStmt(
                        new UnaryExpr(new NameExpr("_assert_counter_"), UnaryExpr.Operator.POSTFIX_INCREMENT)
                );
                parentBlock.addStatement(idx + 1, incrementCounter);
                IfStmt ifStmt = new IfStmt(condition, new ExpressionStmt(assignExpr), null);
                parentBlock.addStatement(idx + 2, ifStmt);
            }

            String varName_temp;
            if (isInLoop) {
                varName_temp = "_assert_var_";
            } else {
                varName_temp = varName;
            }
            addVerify(body, varName,idx, isInLoop);
        }

        private void addVerify(BlockStmt body, String varName, int index,boolean isInLoop) {

            for (ReturnStmt ret : body.findAll(ReturnStmt.class)) {
                if (ret.getExpression().isPresent()) {
                    // return; (void) — skip or handle separately
                    continue;
                }
                BlockStmt pBlock = ret.findAncestor(BlockStmt.class).get();
                int idx = pBlock.getStatements().indexOf(ret);
                pBlock.addStatement(idx, makeVerifyStmt(READABLE_ACCESS, varName));
            }

            boolean verifyExists = body.getStatements().stream()
                    .filter(Statement::isExpressionStmt)
                    .map(s -> s.asExpressionStmt().getExpression())
                    .filter(Expression::isMethodCallExpr)
                    .map(Expression::asMethodCallExpr)
                    .anyMatch(mc -> mc.getNameAsString().equals("verify")
                            && mc.getArguments().size() == 2
                            && mc.getArgument(1).toString().equals(varName));

            if (!verifyExists) {
                if (isCheckingException(body)) {
                    String line_no = FixedStateUtils.getLastLineSuffix(new File(fixedStateFile));
                    // heuristics: add this earlier than exception, but later than the assignment statemetn.
                    insertBeforeLine(body, Integer.parseInt(line_no), makeVerifyStmt(READABLE_ACCESS, varName),index, isInLoop);
                } else {
                    body.addStatement(makeVerifyStmt(READABLE_ACCESS, varName));
                }

            }
        }


        // --- helper methods (instrumentDeclaration, instrumentAssignment, splitOutExpr, addVerify)
        // same as before ...
    }

    private static String baseTypeName(Type type) {
        if (type.isArrayType()) {
            return baseTypeName(type.asArrayType().getComponentType()) + "[]";
        } else if (type.isClassOrInterfaceType()) {
            return type.asClassOrInterfaceType().getNameAsString();
        } else if (type.isPrimitiveType()) {
            return type.asPrimitiveType().asString();
        } else if (type.isVarType()) {
            return "var";
        } else {
            return type.asString(); // fallback
        }
    }




}


