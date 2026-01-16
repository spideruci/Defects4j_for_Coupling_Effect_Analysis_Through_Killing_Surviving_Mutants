// Instrumented at 2025-12-11 17:25:25
package com.fasterxml.jackson.databind.filter;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Unit tests for checking that alternative settings for
 * {@link JsonSerialize#include} annotation property work
 * as expected.
 */
public class JsonInclude1327Test extends BaseMapTest {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class Issue1327BeanEmpty {

        public List<String> myList = new ArrayList<String>();
    }

    static class Issue1327BeanAlways {

        @JsonInclude(JsonInclude.Include.ALWAYS)
        public List<String> myList = new ArrayList<String>();
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    // for [databind#1327]
    public void testClassDefaultsForEmpty() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper om = __ins_v1;
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final String jsonString = om.writeValueAsString(new Issue1327BeanEmpty());
        if (jsonString.contains("myList")) {
            fail("Should not contain `myList`: " + jsonString);
        }
        org.helper.Assertions.verify("var.DEFAULT_BASE._dateFormat_1027_", __ins_v1);
    }

    public void testClassDefaultsForAlways() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        final String jsonString = om.writeValueAsString(new Issue1327BeanAlways());
        if (!jsonString.contains("myList")) {
            fail("Should contain `myList` with Include.ALWAYS: " + jsonString);
        }
    }
}
