package org.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
            ProcessBuilder pb = new ProcessBuilder("python3", script, arg);
            pb.redirectErrorStream(true); // combine stderr with stdout
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            String lastLine = null;

            // read all lines from Python output
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lastLine = line.trim();
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Python process exited with code: " + exitCode);

            if (lastLine != null) {
//                // write last line to file
//                String filePath = "output.txt";
//                BufferedWriter writer = null;
//                try {
//                    writer = new BufferedWriter(new FileWriter(filePath, false));
//                    writer.write(lastLine);
//                    writer.newLine();
//                    System.out.println("Message written to " + filePath);
//                } catch (IOException t) {
//                    t.printStackTrace();
//                } finally {
//                    if (writer != null) {
//                        try {
//                            writer.close();
//                        } catch (IOException t) {
//                            t.printStackTrace();
//                        }
//                    }
//                }

                return lastLine.equalsIgnoreCase("true");
            }
        } catch (Exception e) {
            System.err.println("Python script crashed: " + e.getMessage());
        }

        return false; // default if script failed or returned not "true"
    }

}
