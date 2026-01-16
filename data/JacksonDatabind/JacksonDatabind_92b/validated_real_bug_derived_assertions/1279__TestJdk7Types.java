// Instrumented at 2025-11-28 09:38:30
package com.fasterxml.jackson.databind.ext;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.*;

/**
 * @since 2.7
 */
public class TestJdk7Types extends BaseMapTest {

    public void testPathRoundtrip() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        // Start with serialization, actually
        Path input = Paths.get("tmp", "foo.txt");
        String json = mapper.writeValueAsString(input);
        assertNotNull(json);
        Path p = mapper.readValue(json, Path.class);
        assertNotNull(p);
        assertEquals(input.toUri(), p.toUri());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1279_", __ins_v1);
    }
}
