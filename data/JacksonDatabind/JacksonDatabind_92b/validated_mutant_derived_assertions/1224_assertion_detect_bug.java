// Instrumented at 2025-12-01 00:17:11
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

/**
 * Tests for checking that static methods are not recognized as accessors
 * for properties
 */
public class TestStatics extends BaseMapTest {

    static class Bean {

        int _x;

        public static void setX(int value) {
            throw new Error("Should NOT call static method");
        }

        @JsonProperty("x")
        public void assignX(int x) {
            _x = x;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    public void testSimpleIgnore() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper m = __ins_v1;
        // should not care about static setter...
        Bean result = m.readValue("{ \"x\":3}", Bean.class);
        assertEquals(3, result._x);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1846_32", __ins_v1);
    }
}
