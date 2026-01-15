package org.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class FixedStateUtils {

    /**
     * Reads the given fixed state JSON file and extracts the second part of
     * the "line" field from the last element in the "metas" array.
     *
     *
     * @param fixedStateFile the JSON file to read
     * @return the second part of the "line" field (after the '-')
     * @throws IOException if reading fails
     * @throws IllegalStateException if the JSON structure is invalid
     */
    public static String getLastLineSuffix(File fixedStateFile){
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(fixedStateFile);
            // Step 1: Get the "metas" array
            JsonNode metas = root.path("metas");
            if (!metas.isArray() || metas.isEmpty()) {
                throw new IllegalStateException("No 'metas' array found or it's empty!");
            }

            // Step 2: Get the last element
            JsonNode lastMeta = metas.get(metas.size() - 1);

            // Step 3: Get the "line" field
            String line = lastMeta.path("line").asText(null);
            if (line == null || line.isEmpty()) {
                throw new IllegalStateException("Missing 'line' field in the last meta element!");
            }

            // Step 4: Extract second part
            String[] parts = line.split("-");
            if (parts.length < 2) {
                throw new IllegalStateException("Invalid 'line' format: " + line);
            }

            return parts[1];
        } catch (IOException e) {
            throw new RuntimeException("Failed to read fixed state file: " + fixedStateFile, e);
        }
    }

    // Example usage
    public static void main(String[] args) throws IOException {
        File file = new File("fixedStateFile.json");
        String suffix = getLastLineSuffix(file);
        System.out.println("Extracted line suffix: " + suffix);
    }
}
