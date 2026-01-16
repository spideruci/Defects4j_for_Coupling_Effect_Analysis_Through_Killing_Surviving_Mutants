// Instrumented at 2025-11-28 09:38:32
package com.fasterxml.jackson.databind.creators;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class Creator1476Test extends BaseMapTest {

    static final class SimplePojo {

        private final int intField;

        private final String stringField;

        public SimplePojo(@JsonProperty("intField") int intField) {
            this(intField, "empty");
        }

        public SimplePojo(@JsonProperty("stringField") String stringField) {
            this(-1, stringField);
        }

        @JsonCreator
        public SimplePojo(@JsonProperty("intField") int intField, @JsonProperty("stringField") String stringField) {
            this.intField = intField;
            this.stringField = stringField;
        }

        public int getIntField() {
            return intField;
        }

        public String getStringField() {
            return stringField;
        }
    }

    public void testConstructorChoice() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        SimplePojo pojo = mapper.readValue("{ \"intField\": 1, \"stringField\": \"foo\" }", SimplePojo.class);
        assertEquals(1, pojo.getIntField());
        assertEquals("foo", pojo.getStringField());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1151_", __ins_v1);
    }
}
