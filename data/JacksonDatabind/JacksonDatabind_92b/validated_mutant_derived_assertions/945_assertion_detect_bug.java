// Instrumented at 2025-12-01 00:17:09
package com.fasterxml.jackson.databind.creators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

/**
 * Tests to ensure one can disable {@link JsonCreator} annotations.
 */
public class DisablingCreatorsTest extends BaseMapTest {

    static class ConflictingCreators {

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public ConflictingCreators(@JsonProperty("foo") String foo) {
        }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public ConflictingCreators(@JsonProperty("foo") String foo, @JsonProperty("value") int value) {
        }
    }

    static class NonConflictingCreators {

        public String _value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public NonConflictingCreators(String foo) {
            _value = foo;
        }

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public NonConflictingCreators(String foo, int value) {
        }
    }

    /*
     /**********************************************************
     /* Helper methods
     /**********************************************************
      */
    public void testDisabling() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = objectMapper();
        final ObjectMapper mapper = __ins_v1;
        // first, non-problematic case
        NonConflictingCreators value = mapper.readValue(quote("abc"), NonConflictingCreators.class);
        assertNotNull(value);
        assertEquals("abc", value._value);
        // then something that ought to fail
        try {
            /*ConflictingCreators value =*/
            mapper.readValue(quote("abc"), ConflictingCreators.class);
            fail("Should have failed with JsonCreator conflict");
        } catch (JsonProcessingException e) {
            verifyException(e, "Conflicting property-based creators");
        }
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1596_32", __ins_v1);
    }
}
