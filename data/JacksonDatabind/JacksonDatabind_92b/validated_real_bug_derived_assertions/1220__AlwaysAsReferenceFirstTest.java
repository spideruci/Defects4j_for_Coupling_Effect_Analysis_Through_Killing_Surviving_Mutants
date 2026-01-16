// Instrumented at 2025-11-28 09:38:33
package com.fasterxml.jackson.databind.objectid;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class AlwaysAsReferenceFirstTest extends BaseMapTest {

    // [databind#1255]
    @JsonPropertyOrder({ "bar1", "bar2" })
    static class Foo {

        @JsonIdentityReference(alwaysAsId = true)
        public Bar bar1;

        @JsonIdentityReference
        public Bar bar2;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
    static class Bar {

        public int value = 3;
    }

    // [databind#1607]
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class Value1607 {

        public int value;

        public Value1607() {
            this(0);
        }

        public Value1607(int v) {
            value = v;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    static class Value1607ViaClass {

        public int value;

        public Value1607ViaClass() {
            this(0);
        }

        public Value1607ViaClass(int v) {
            value = v;
        }
    }

    @JsonPropertyOrder(alphabetic = true)
    static class ReallyAlwaysContainer {

        public Value1607ViaClass alwaysClass = new Value1607ViaClass(13);

        @JsonIdentityReference(alwaysAsId = true)
        public Value1607 alwaysProp = new Value1607(13);
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    private final ObjectMapper MAPPER = new ObjectMapper();

    // [databind#1255]
    public void testIssue1255() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;
        __ins_v1 = MAPPER;
        String json = __ins_v1.writeValueAsString(mo);
        Foo result = MAPPER.readValue(json, Foo.class);
        assertNotNull(result);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1220_", __ins_v1);
    }

    // [databind#1607]
    public void testIssue1607() throws Exception {
        String json = MAPPER.writeValueAsString(new ReallyAlwaysContainer());
        assertEquals(aposToQuotes("{'alwaysClass':1,'alwaysProp':2}"), json);
    }
}
