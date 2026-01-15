package org.helper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class JsonReader {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Adjust the file path
        File file = new File("oracle specification/assertion_0.json");

        Spec spec = mapper.readValue(file, Spec.class);

        System.out.println("Loaded spec:");
        System.out.println(spec);

        // Example: access python_access
        for (Object seg : spec.python_access) {
            System.out.println("Path segment: " + seg);
        }
    }
}
