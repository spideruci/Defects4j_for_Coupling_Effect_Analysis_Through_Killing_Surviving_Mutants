// Instrumented at 2025-12-13 14:00:50
package com.fasterxml.jackson.databind.convert;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

/**
 * Unit tests for verifying handling of update value on polymorphic
 * objects.
 */
public class TestPolymorphicUpdateValue extends BaseMapTest {

    @//PROPERTY
    JsonTypeInfo(//PROPERTY
    include = JsonTypeInfo.As.WRAPPER_ARRAY, use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(value = { @JsonSubTypes.Type(value = Child.class) })
    abstract static class Parent {

        public int x;

        public int y;
    }

    @JsonTypeName("child")
    public static class Child extends Parent {

        public int w;

        public int h;
    }

    /*
    /********************************************************
    /* Unit tests
    /********************************************************
     */
    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testPolymorphicTest() throws Exception {
        com.fasterxml.jackson.databind.ObjectReader __ins_v1 = null;
        Child c = new Child();
        c.w = 10;
        c.h = 11;
        __ins_v1 = MAPPER.readerForUpdating(c);
        __ins_v1.readValue("{\"x\":3,\"y\":4,\"w\":111}");
        assertEquals(3, c.x);
        assertEquals(4, c.y);
        assertEquals(111, c.w);
        org.helper.Assertions.verify("var._context._factory.DEFAULT_NO_DESER_CLASS_NAMES_2611_18", __ins_v1);
    }
}
