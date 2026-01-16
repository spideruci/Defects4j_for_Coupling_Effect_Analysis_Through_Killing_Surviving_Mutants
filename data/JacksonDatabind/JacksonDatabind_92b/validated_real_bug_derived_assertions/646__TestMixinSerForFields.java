// Instrumented at 2025-11-28 09:38:36
package com.fasterxml.jackson.databind.mixins;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class TestMixinSerForFields extends BaseMapTest {

    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */
    static class BaseClass {

        public String a;

        protected String b;

        public BaseClass(String a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    static class SubClass extends BaseClass {

        public SubClass(String a, String b) {
            super(a, b);
        }
    }

    abstract class MixIn {

        // Let's add 'b' as "banana"
        @JsonProperty("banana")
        public String b;
    }

    abstract class MixIn2 {

        // Let's remove 'a'
        @JsonIgnore
        public String a;

        // also: add a dummy field that is NOT to match anything
        @JsonProperty
        public String xyz;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    public void testFieldMixInsTopLevel() throws IOException {
        ObjectMapper __ins_v1 = null;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result;
        BaseClass bean = new BaseClass("1", "2");
        // first: with no mix-ins:
        result = writeAndMap(mapper, bean);
        assertEquals(1, result.size());
        assertEquals("1", result.get("a"));
        __ins_v1 = new ObjectMapper();
        // and then with simple mix-in
        mapper = __ins_v1;
        mapper.addMixIn(BaseClass.class, MixIn.class);
        result = writeAndMap(mapper, bean);
        assertEquals(2, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("banana"));
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_646_", __ins_v1);
    }

    public void testMultipleFieldMixIns() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // ordering here shouldn't matter really...
        HashMap<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
        mixins.put(SubClass.class, MixIn.class);
        mixins.put(BaseClass.class, MixIn2.class);
        mapper.setMixIns(mixins);
        Map<String, Object> result;
        result = writeAndMap(mapper, new SubClass("1", "2"));
        assertEquals(1, result.size());
        // 'a' should be suppressed; 'b' mapped to 'banana'
        assertEquals("2", result.get("banana"));
    }
}
