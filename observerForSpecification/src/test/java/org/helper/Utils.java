package org.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    public static boolean checkFileExist(String path) {
        return Files.exists(Paths.get(path));
    }

    public static Set<String> readLinesFromFileToSet(String path) {
        Set<String> lines = new HashSet<>();
        try {
            lines = Files.lines(Paths.get(path)).collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void addErrorMessage(String message) {
        TestExtension.appendToFile("initialize/errorMessages.txt", TestExtension.testID + "\n" + message);
    }

    public static String getStackTraceInfo(Exception e) {
        StringBuilder sb = new StringBuilder();
        if (e.getLocalizedMessage()!= null)
            sb.append(e.getLocalizedMessage());
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
