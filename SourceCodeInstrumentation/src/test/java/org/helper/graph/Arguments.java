package org.helper.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Arguments {

    // control the [depth] of the object graph
    public static int depth = 5;

    // control the [timing] of the state dump
    public static boolean multiDump = true;
    public static int numDumps = 1;

    // control the [scope element] of the state dump
    public static boolean dumpThis = true;
    public static boolean dumpStatic = true;

    // control the [scope] of the state dump in instrumentation

    // initial scan, record the number of objects recorded for each test run
    public static boolean isInitialScan = false;

    public static Map<String, Integer> stateNumMap = new HashMap<String, Integer>();

    static {
        checkArguments();
        String filePath = "initialize/stateNum.txt";
        if (!isInitialScan && multiDump && fileExists(filePath)) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(filePath));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] result = line.split("---", 2); // keep key---value format
                    if (result.length == 2) {
                        stateNumMap.put(result[0], Integer.valueOf(result[1]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (IOException ignore) {}
                }
            }
        }
    }
//
//
//    static {
//        checkArguments();
//        String filePath = "initialize/stateNum.txt";
//        if (!isInitialScan && multiDump && fileExists(filePath) ) {
//            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    String[] result = line.split("---");
//                    stateNumMap.put(result[0], Integer.parseInt(result[1]));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            // read the test runs' number of states from the file
//        }
//    }

    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static void checkArguments() {

    }




}
