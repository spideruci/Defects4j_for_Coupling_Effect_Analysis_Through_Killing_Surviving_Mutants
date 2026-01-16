// Instrumented at 2025-12-01 00:16:56
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

/**
 * Tests for checking handling of abstract types.
 */
public class TestAbstract extends BaseMapTest {

    static abstract class Abstract {

        public int x;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    /**
     * Test to verify details of how trying to deserialize into
     * abstract type should fail (if there is no way to determine
     * actual type information for the concrete type to use)
     */
    public void testAbstractFailure() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper m = __ins_v1;
        try {
            m.readValue("{ \"x\" : 3 }", Abstract.class);
            fail("Should fail on trying to deserialize abstract type");
        } catch (JsonProcessingException e) {
            verifyException(e, "can not construct");
        }
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_783_32", __ins_v1);
    }
}
