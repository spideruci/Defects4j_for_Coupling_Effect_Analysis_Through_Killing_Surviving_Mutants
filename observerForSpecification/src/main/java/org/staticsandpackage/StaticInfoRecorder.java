package org.staticsandpackage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.staticsandpackage.Utils.getStackTraceInfo;

public class StaticInfoRecorder {
    public static Set<String> fields = new HashSet<String>();

    public static void clearStaticFields() {
        generateDirs("initialize/");
        try {
            Files.write(Paths.get("initialize/staticFields.txt"), new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFieldsToFile() {
        generateDirs("initialize/");
        createFile("initialize/staticFields.txt");
        for (String field : fields) {
            appendToFile("initialize/staticFields.txt", field.replace("/", "."));
        }
    }

    public static void appendToFile(String filePath, String textToAppend) {
//        System.err.println(filePath);
        try (FileWriter writer = new FileWriter(filePath, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            // Append the text
            bufferedWriter.write(textToAppend);
            bufferedWriter.newLine(); // This will add a new line after the text
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFile(String filePath) {
//        System.err.println(filePath);
        try (FileWriter writer = new FileWriter(filePath, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void generateDirs(String dir) {
        Path path = Paths.get(dir);
        try {
            // Create directories
            Files.createDirectories(path);
        } catch (Exception e) {
//            System.err.println("Error occurred while creating directories: " + getStackTraceInfo(e));
        }
    }

}

