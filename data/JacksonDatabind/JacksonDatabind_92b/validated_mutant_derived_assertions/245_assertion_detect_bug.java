// Instrumented at 2025-12-01 00:16:58
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

    // [Issue#318] (and Scala module issue #83]
    public void testValueUpdateWithCreator() throws Exception {
        com.fasterxml.jackson.databind.ObjectReader __ins_v1 = null;
        Bean bean = new Bean("abc", "def");
        __ins_v1 = new ObjectMapper().readerFor(Bean.class);
        __ins_v1.withValueToUpdate(bean).readValue("{\"a\":\"ghi\",\"b\":\"jkl\"}");
        assertEquals("ghi", bean.getA());
        assertEquals("jkl", bean.getB());
        org.helper.Assertions.verify("var._context._factory.DEFAULT_NO_DESER_CLASS_NAMES_1546_32", __ins_v1);
    }
}
