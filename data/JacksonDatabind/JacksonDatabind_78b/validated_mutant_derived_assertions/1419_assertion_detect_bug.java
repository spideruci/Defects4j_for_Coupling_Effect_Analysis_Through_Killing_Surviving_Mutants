// Instrumented at 2025-12-13 14:00:49
package com.fasterxml.jackson.databind.creators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;

public class TestValueUpdate extends BaseMapTest {

    static class Bean {

        private String a;

        private String b;

        @JsonCreator
        public Bean(@JsonProperty("a") String a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }

        String getA() {
            return a;
        }

        void setA(String a) {
            this.a = a;
        }

        String getB() {
            return b;
        }

        void setB(String b) {
            this.b = b;
        }
    }

    private final ObjectMapper MAPPER = new ObjectMapper();

    // [databind#318] (and Scala module issue #83]
    public void testValueUpdateWithCreator() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        Bean bean = new Bean("abc", "def");
        __ins_v1 = MAPPER;
        __ins_v1.readerFor(Bean.class).withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_3309_18", __ins_v1);
    }

    public void testValueUpdateOther() throws Exception {
        Bean bean = new Bean("abc", "def");
        ObjectReader r = MAPPER.reader().withValueToUpdate(bean);
        // but, changed our minds, no update
        r = r.withValueToUpdate(null);
    }
}
