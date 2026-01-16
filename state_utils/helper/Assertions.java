package org.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.helper.states.Loc;
import org.helper.states.StateItem;
import org.helper.states.type;
import org.helper.graph.TesExporter;
//import com.github.javaparser.StaticJavaParser;
//import com.github.javaparser.ast.CompilationUnit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Assertions {
    public static void verify(String message, Object obj) {
//         1. serialize obj to json;
        StateItem stateitem = new StateItem(new Loc("o", "o"), type.Object, "", obj, 0);
        try {
            String json = TesExporter.exportVar(stateitem,  5, 5);
            writeActualOutputToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to serialize the object in assertions",e);
        }

        // 2. extract the corresponding node from the access path for the obj.

        String[] parts = message.split("_");

        String last = parts[parts.length - 1];
        String secondLast = parts.length >= 2 ? parts[parts.length - 2] : last;

        String oracle_index = "";
        // Check if the second last is a number
        if (secondLast.matches("\\d+")) {
            oracle_index = secondLast + ".json";
        } else {
            oracle_index = last + ".json";
        }

        boolean result = RunPythonFromJava.runPythonScript("verifyOracleData.py", oracle_index);
        if (!result) {
            throw new AssertionError("assertion failed!");
        }

        // TODO 2 [Run the python code] extract the corresponding node from the access path for the obj.
        // TODO 3 [Run the python code] extract the proper json node from the pre-recorded state file on fixed version of the code
        // and verify the the json with the message.
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
