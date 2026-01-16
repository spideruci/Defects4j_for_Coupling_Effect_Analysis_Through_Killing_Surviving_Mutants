// Instrumented at 2025-11-28 09:38:35
package com.fasterxml.jackson.databind.ser;

import java.util.*;
import com.fasterxml.jackson.databind.*;

/**
 * Simple unit tests to verify that it is possible to handle
 * potentially cyclic structures, as long as object graph itself
 * is not cyclic. This is the case for directed hierarchies like
 * trees and DAGs.
 */
public class TestCyclicTypes extends BaseMapTest {

    /*
    /**********************************************************
    /* Helper bean classes
    /**********************************************************
     */
    static class Bean {

        Bean _next;

        final String _name;

        public Bean(Bean next, String name) {
            _next = next;
            _name = name;
        }

        public Bean getNext() {
            return _next;
        }

        public String getName() {
            return _name;
        }

        public void assignNext(Bean n) {
            _next = n;
        }
    }

    /*
    /**********************************************************
    /* Types
    /**********************************************************
     */
    public void testLinked() throws Exception {
        Bean last = new Bean(null, "last");
        Bean first = new Bean(last, "first");
        Map<String, Object> map = writeAndMap(new ObjectMapper(), first);
        assertEquals(2, map.size());
        assertEquals("first", map.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> map2 = (Map<String, Object>) map.get("next");
        assertNotNull(map2);
        assertEquals(2, map2.size());
        assertEquals("last", map2.get("name"));
        assertNull(map2.get("next"));
    }

    /**
     * Test for verifying that [JACKSON-158] works as expected
     */
    public void testSelfReference() throws Exception {
        ObjectMapper __ins_v1 = null;
        Bean selfRef = new Bean(null, "self-refs");
        Bean first = new Bean(selfRef, "first");
        selfRef.assignNext(selfRef);
        __ins_v1 = new ObjectMapper();
        ObjectMapper m = __ins_v1;
        Bean[] wrapper = new Bean[] { first };
        try {
            writeAndMap(m, wrapper);
        } catch (JsonMappingException e) {
            verifyException(e, "Direct self-reference leading to cycle");
        }
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_345_", __ins_v1);
    }
}
