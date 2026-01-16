// Instrumented at 2025-12-13 09:03:22
package com.fasterxml.jackson.databind.ext;

import java.util.Deque;
import java.util.NavigableSet;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests to ensure that we can handle 1.6-only types, even if
 * registrations are done without direct refs
 */
public class TestJdk16Types extends com.fasterxml.jackson.databind.BaseMapTest {

    // for [databind#216]
    public void test16Types() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        final ObjectMapper mapper = __ins_v1;
        Deque<?> dq = mapper.readValue("[1]", Deque.class);
        assertNotNull(dq);
        assertEquals(1, dq.size());
        assertTrue(dq instanceof Deque<?>);
        NavigableSet<?> ns = mapper.readValue("[ true ]", NavigableSet.class);
        assertEquals(1, ns.size());
        assertTrue(ns instanceof NavigableSet<?>);
        org.helper.Assertions.verify("var._rootDeserializers_37_244", __ins_v1);
    }
}
