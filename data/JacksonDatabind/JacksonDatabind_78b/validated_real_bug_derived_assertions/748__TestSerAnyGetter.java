// Instrumented at 2025-12-10 19:59:23
package com.fasterxml.jackson.databind.access;

import java.util.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

/**
 * Separate tests located in different package than code being
 * exercised; needed to trigger some access-related failures.
 */
public class TestSerAnyGetter extends BaseMapTest {

    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */
    static class DynaBean {

        public int id;

        protected HashMap<String, String> other = new HashMap<String, String>();

        @JsonAnyGetter
        public Map<String, String> any() {
            return other;
        }

        @JsonAnySetter
        public void set(String name, String value) {
            other.put(name, value);
        }
    }

    static class PrivateThing {

        @JsonAnyGetter
        public Map<?, ?> getProperties() {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("a", "A");
            return map;
        }
    }

    /*
    /**********************************************************
    /* Test cases
    /**********************************************************
     */
    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testDynaBean() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        DynaBean b = new DynaBean();
        b.id = 123;
        b.set("name", "Billy");
        __ins_v1 = MAPPER;
        assertEquals("{\"id\":123,\"name\":\"Billy\"}", __ins_v1.writeValueAsString(b));
        DynaBean result = MAPPER.readValue("{\"id\":2,\"name\":\"Joe\"}", DynaBean.class);
        assertEquals(2, result.id);
        assertEquals("Joe", result.other.get("name"));
        org.helper.Assertions.verify("var._deserializationContext._factory_748_", __ins_v1);
    }

    public void testPrivate() throws Exception {
        String json = MAPPER.writeValueAsString(new PrivateThing());
        assertEquals("{\"a\":\"A\"}", json);
    }
}
