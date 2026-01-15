package org.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RunPythonFromJava {
//    public static void main(String[] args) {
//        String pythonScript = "check.py";  // path to your Python script
//        String argument = "ok";            // example argument
//
//        boolean result = runPythonScript(pythonScript, argument);
//        System.out.println("Final result: " + result);
//    }

    public static boolean runPythonScript(String script, String arg) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", script, arg);
            pb.redirectErrorStream(true); // combine stderr with stdout
            Process process = pb.start();

            // Capture the output
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line = reader.readLine();
            process.waitFor();

            if (line != null && line.trim().equalsIgnoreCase("true")) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Python script crashed: " + e.getMessage());
        }
        return false; // default if script failed or returned "False"
    }
}
