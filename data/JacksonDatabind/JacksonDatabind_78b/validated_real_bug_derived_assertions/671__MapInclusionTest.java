// Instrumented at 2025-12-10 19:59:28
package com.fasterxml.jackson.databind.filter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;

public class MapInclusionTest extends BaseMapTest {

    static class NoEmptiesMapContainer {

        @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_EMPTY)
        public Map<String, String> stuff = new LinkedHashMap<String, String>();

        public NoEmptiesMapContainer add(String key, String value) {
            stuff.put(key, value);
            return this;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    final private ObjectMapper MAPPER = objectMapper();

    // [databind#588]
    public void testNonNullValueMapViaProp() throws IOException {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = MAPPER;
        String json = __ins_v1.writeValueAsString(new NoEmptiesMapContainer().add("a", null).add("b", ""));
        assertEquals(aposToQuotes("{}"), json);
        org.helper.Assertions.verify("var._deserializationContext._factory_671_", __ins_v1);
    }
}
