// Instrumented at 2025-12-13 14:00:51
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
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        String[] strs = new String[] { "abc" };
        __ins_v1 = MAPPER;
        assertEquals("[\"abc\"]", __ins_v1.writeValueAsString(new AtomicReference<String[]>(strs)));
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_2738_18", __ins_v1);
    }

    public void testCustomSerializer() throws Exception {
        final String VALUE = "fooBAR";
        String json = MAPPER.writeValueAsString(new UCStringWrapper(VALUE));
        assertEquals(json, aposToQuotes("{'value':'FOOBAR'}"));
    }
}
