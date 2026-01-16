// Instrumented at 2025-11-28 09:38:37
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.*;

// mostly for [databind#1033]
public class SetterConflictTest extends BaseMapTest {

    // Should prefer primitives over Strings, more complex types, by default
    static class Issue1033Bean {

        public int value;

        public void setValue(int v) {
            value = v;
        }

        public void setValue(Issue1033Bean foo) {
            throw new Error("Should not get called");
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    private final ObjectMapper MAPPER = objectMapper();

    public void testSetterPriority() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = MAPPER;
        Issue1033Bean bean = __ins_v1.readValue(aposToQuotes("{'value':42}"), Issue1033Bean.class);
        assertEquals(42, bean.value);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1159_", __ins_v1);
    }
}
