// Instrumented at 2025-12-10 19:59:21
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
        Bean bean = new Bean("abc", "def");
        MAPPER.readerFor(Bean.class).withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
    }

    public void testValueUpdateOther() throws Exception {
        com.fasterxml.jackson.databind.ObjectReader __ins_v1 = null;
        Bean bean = new Bean("abc", "def");
        __ins_v1 = MAPPER.reader().withValueToUpdate(bean);
        ObjectReader r = __ins_v1;
        // but, changed our minds, no update
        r = r.withValueToUpdate(null);
        org.helper.Assertions.verify("var._context._factory_1062_", __ins_v1);
    }
}
