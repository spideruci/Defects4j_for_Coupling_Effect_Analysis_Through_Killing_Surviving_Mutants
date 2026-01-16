// Instrumented at 2025-12-10 19:59:25
package com.fasterxml.jackson.databind.deser.exc;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class ExceptionPathTest extends BaseMapTest {

    static class Outer {

        public Inner inner = new Inner();
    }

    static class Inner {

        public int x;

        @JsonCreator
        public static Inner create(@JsonProperty("x") int x) {
            throw new RuntimeException("test-exception");
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testReferenceChainForInnerClass() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = MAPPER;
        String json = __ins_v1.writeValueAsString(new Outer());
        try {
            MAPPER.readValue(json, Outer.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            JsonMappingException.Reference reference = e.getPath().get(0);
            assertEquals(getClass().getName() + "$Outer[\"inner\"]", reference.toString());
        }
        org.helper.Assertions.verify("var._deserializationContext._factory_1237_", __ins_v1);
    }

    public static void main(String[] args) {
        System.err.println("Int, full: " + Integer.TYPE.getName());
    }
}
