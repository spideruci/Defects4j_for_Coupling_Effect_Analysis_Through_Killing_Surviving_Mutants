package org.instrumentation.group0instrumentor;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static int calculateParaNum(String methodDesc){

        //There is something wrong here, but it doesn't matter for now.
        Pattern pattern1 = Pattern.compile("\\(\\)");
        Matcher matcher1 = pattern1.matcher(methodDesc);

        if(matcher1.find()){
            return 0;
        }
        return 1;
    }

    public static void appendToFile(String filename, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(content);
            writer.newLine();  // Add a new line after the content.
        } catch (IOException e) {
            throw new  RuntimeException(e);
        }
    }

    public static void writeFailInfo(String filename, String content) {

        File f = new File(filename);
        try {
            f.createNewFile();
            appendToFile(filename,content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static Boolean isAssertionStatement(String owner,String methodName) {
        // owner.startsWith("org/assertj")
        final boolean b = methodName.startsWith("assert") | (methodName.startsWith("fail"));
        if (owner.startsWith("org/junit/jupiter/api") && b) {
            return true;
        }
        if (owner.startsWith("org/junit/Assert") && b) {
            return true;
        }
        if (owner.startsWith("junit/framework") && b) {
            return true;
        }
        return false;
    }

    public static Boolean isMyAssertionStatement(String owner,String methodName) {
        // owner.startsWith("org/assertj")
        if (owner.startsWith("ProxyAssertions/customized") && methodName.startsWith("runAssertion") ) {
            return true;
        }
        return false;
    }


    public static void appendLogAt(String filename, String[] lines){
        try (FileWriter f = new FileWriter(filename, true);
             BufferedWriter b = new BufferedWriter(f);
             PrintWriter p = new PrintWriter(b);)
        {
            for(String line: lines){
                p.println(line);
            }
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static boolean containsIllegal(String s) {
        for (String illegal : illegalClasses) {
            if (s.contains(illegal)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> illegalClasses = new HashSet<>();
    static {
        illegalClasses.add("org/helper/graph");
        illegalClasses.add("org/helper/states");
        illegalClasses.add("org/helper/InstrumentationUtils");
        illegalClasses.add("org/helper/Utils");
        illegalClasses.add("org/helper/TesExtension");
        illegalClasses.add("org/helper/TesRunner");
        illegalClasses.add("auxiliary/");
        illegalClasses.add("ProxyAssertions/");
    }
}

