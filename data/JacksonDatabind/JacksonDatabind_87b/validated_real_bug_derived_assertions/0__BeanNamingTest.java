// Instrumented at 2025-12-11 17:23:36
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.*;

// Tests for [databind#653]
public class BeanNamingTest extends BaseMapTest {

    static class URLBean {

        public String getURL() {
            return "http://foo";
        }
    }

    static class ABean {

        public int getA() {
            return 3;
        }
    }

    public void testSimple() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        assertFalse(mapper.isEnabled(MapperFeature.USE_STD_BEAN_NAMING));
        assertEquals(aposToQuotes("{'url':'http://foo'}"), mapper.writeValueAsString(new URLBean()));
        assertEquals(aposToQuotes("{'a':3}"), mapper.writeValueAsString(new ABean()));
        mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        assertEquals(aposToQuotes("{'URL':'http://foo'}"), mapper.writeValueAsString(new URLBean()));
        assertEquals(aposToQuotes("{'a':3}"), mapper.writeValueAsString(new ABean()));
        org.helper.Assertions.verify("var.DEFAULT_BASE._dateFormat_0_", __ins_v1);
    }
}
