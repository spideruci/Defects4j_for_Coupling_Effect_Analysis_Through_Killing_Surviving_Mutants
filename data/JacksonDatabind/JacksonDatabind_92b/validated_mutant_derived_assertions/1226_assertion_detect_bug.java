// Instrumented at 2025-12-01 00:17:11
package com.fasterxml.jackson.databind.ser;

import java.util.concurrent.atomic.*;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Unit tests for verifying serialization of simple basic non-structured
 * types; primitives (and/or their wrappers), Strings.
 */
public class TestSimpleAtomicTypes extends BaseMapTest {

    static class UCStringWrapper {

        @JsonSerialize(contentUsing = UpperCasingSerializer.class)
        public AtomicReference<String> value;

        public UCStringWrapper(String s) {
            value = new AtomicReference<String>(s);
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    private final ObjectMapper MAPPER = objectMapper();

    public void testAtomicBoolean() throws Exception {
        assertEquals("true", MAPPER.writeValueAsString(new AtomicBoolean(true)));
        assertEquals("false", MAPPER.writeValueAsString(new AtomicBoolean(false)));
    }

    public void testAtomicInteger() throws Exception {
        assertEquals("1", MAPPER.writeValueAsString(new AtomicInteger(1)));
        assertEquals("-9", MAPPER.writeValueAsString(new AtomicInteger(-9)));
    }

    public void testAtomicLong() throws Exception {
        assertEquals("0", MAPPER.writeValueAsString(new AtomicLong(0)));
    }

    public void testAtomicReference() throws Exception {
        String[] strs = new String[] { "abc" };
        assertEquals("[\"abc\"]", MAPPER.writeValueAsString(new AtomicReference<String[]>(strs)));
    }

    public void testCustomSerializer() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        final String VALUE = "fooBAR";
        __ins_v1 = MAPPER;
        String json = __ins_v1.writeValueAsString(new UCStringWrapper(VALUE));
        assertEquals(json, aposToQuotes("{'value':'FOOBAR'}"));
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1145_32", __ins_v1);
    }
}
