package org.instrumentor;

import java.io.File;

public class TestFileLocator {

    public static File locateTestFile(String testName) {
        // Remove the ::method part if present
        String className = testName.split("::")[0];

        // Convert package-style names (org.example.MyTest) into path (org/example/MyTest)
        String relativePath = className.replace('.', File.separatorChar) + ".java";

        // First possible location: src/test/java
        File file1 = new File("src/test/java", relativePath);
        if (file1.exists()) {
            return file1;
        }

        // Second possible location: src/test
        File file2 = new File("src/test", relativePath);
        if (file2.exists()) {
            return file2;
        }

        //Third possible location: tests
        File file3 = new File("tests", relativePath);
        if (file3.exists()) {
            return file3;
        }

        //Another possible location for Gson
        File file4 = new File("gson/src/test/java", relativePath);
        if (file4.exists()) {
            return file4;
        }


        // If not found, raise an error
        throw new RuntimeException("Test file not found for " + testName +
                " (checked: " + file1.getPath() + " and " + file2.getPath() + ")");
    }

//    public static void main(String[] args) {
//        // Example
//        String testName = "org.apache.commons.cli.BugsTest::test11458";
//        File testFile = locateTestFile(testName);
//        System.out.println("Found test file: " + testFile.getAbsolutePath());
//    }
}

