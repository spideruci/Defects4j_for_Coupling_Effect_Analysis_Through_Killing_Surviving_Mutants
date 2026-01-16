// Instrumented at 2025-11-28 09:38:32
package com.fasterxml.jackson.databind.introspect;

import java.beans.Transient;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.*;

/**
 * Tests for both `transient` keyword and JDK 7
 * {@link java.beans.Transient} annotation.
 */
public class TransientTest extends BaseMapTest {

    // for [databind#296]
    @JsonPropertyOrder({ "x" })
    static class ClassyTransient {

        public transient int value = 3;

        public int getValue() {
            return value;
        }

        public int getX() {
            return 42;
        }
    }

    // for [databind#857]
    static class BeanTransient {

        @Transient
        public int getX() {
            return 3;
        }

        public int getY() {
            return 4;
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    private final ObjectMapper MAPPER = objectMapper();

    // for [databind#296]
    public void testTransientFieldHandling() throws Exception {
        // default handling: remove transient field but do not propagate
        assertEquals(aposToQuotes("{'x':42,'value':3}"), MAPPER.writeValueAsString(new ClassyTransient()));
        // but may change that
        ObjectMapper m = new ObjectMapper().enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        assertEquals(aposToQuotes("{'x':42}"), m.writeValueAsString(new ClassyTransient()));
    }

    // for [databind#857]
    public void testBeanTransient() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = MAPPER;
        assertEquals(aposToQuotes("{'y':4}"), __ins_v1.writeValueAsString(new BeanTransient()));
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1521_", __ins_v1);
    }
}
