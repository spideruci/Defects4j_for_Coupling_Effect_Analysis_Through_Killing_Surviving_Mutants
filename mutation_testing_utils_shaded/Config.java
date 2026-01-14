// This package name is required by Major!
package major.mutation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import com.fasterxml_shaded.jackson.databind.*;
import com.fasterxml_shaded.jackson.databind.node.*;
import java.io.*;
import java.util.Arrays;

/**
 * A simple driver class for Major --
 * this class name is required by Major!
 */
public class Config {


    /*
     * The mutant identifier:
     *
     * __M_NO <  0 -> Run original version
     *
     * __M_NO == 0 -> Run original version and gather coverage information
     *
     * __M_NO >  0 -> Execute mutant with the corresponding id
     */
    public static int __M_NO = 0;

    // Set to store IDs of covered mutants
    public static BitSet covSet = new BitSet();

    // The coverage method is called if and only if the
    // mutant identifier is set to 0!
    public static boolean COVERED(int from, int to) {
//        return true;
        synchronized (covSet) {
            covSet.set(from, to + 1);
        }
//        // Always return false as required by
//        // Conditional Mutation!
        return false;
    }

    /*
     * Additional methods for the mutation analysis back-end
     */
    // Reset the coverage information
    public static void reset() {
        synchronized (covSet) {
            covSet.clear();
        }
    }

    // Get list of all covered mutants
    public static List<Integer> getCoverageList() {
        synchronized (covSet) {
            List<Integer> covList = new ArrayList<Integer>(covSet.cardinality());
            for (int i = covSet.nextSetBit(0); i >= 0; i = covSet.nextSetBit(i+1)) {
                covList.add(i);
            }

            return covList;
        }
    }

    static {
        if (__M_NO == 0) {
            // Register a shutdown hook that runs at JVM exit
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {


                    try {
                        File file = new File("mutant_coverage.json");

                        // Step 1: Read JSON as an array
                        ObjectMapper mapper = new ObjectMapper();
                        ArrayNode root = (ArrayNode) mapper.readTree(file);

                        if (root.size() == 0) {
                            System.out.println("JSON array is empty!");
                            return;
                        }

                        // Step 2: Get the last object in the array
                        ObjectNode lastEntry = (ObjectNode) root.get(root.size() - 1);

                        // Step 3: Get or create the "mutant_ids" array
                        ArrayNode mutantIds = (ArrayNode) lastEntry.withArray("mutant_ids");

                        // Step 4: Add numbers (example: 101, 102, 103)

                        for (int id : getCoverageList()) {
                            mutantIds.add(id);
                        }

                        // Step 5: Write back to the file (pretty-printed)
                        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

                        System.out.println("✅ Added mutant IDs to last entry!");

                    } catch (Throwable e) {
                        throw new RuntimeException("Error updating JSON file", e);
                    }


                }
            }));
        }
    }
}
