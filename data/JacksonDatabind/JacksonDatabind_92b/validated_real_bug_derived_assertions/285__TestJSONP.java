// Instrumented at 2025-11-28 09:38:34
package com.fasterxml.jackson.databind.misc;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.JSONPObject;

public class TestJSONP extends BaseMapTest {

    static class Base {

        public String a;
    }

    static class Impl extends Base {

        public String b;

        public Impl(String a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    public void testSimpleScalars() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertEquals("callback(\"abc\")", serializeAsString(m, new JSONPObject("callback", "abc")));
        assertEquals("calc(123)", serializeAsString(m, new JSONPObject("calc", Integer.valueOf(123))));
        assertEquals("dummy(null)", serializeAsString(m, new JSONPObject("dummy", null)));
    }

    public void testSimpleBean() throws Exception {
        ObjectMapper m = new ObjectMapper();
        assertEquals("xxx({\"a\":\"123\",\"b\":\"456\"})", serializeAsString(m, new JSONPObject("xxx", new Impl("123", "456"))));
    }

    /**
     * Test to ensure that it is possible to force a static type for wrapped
     * value.
     */
    public void testWithType() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper m = __ins_v1;
        Object ob = new Impl("abc", "def");
        JavaType type = m.constructType(Base.class);
        assertEquals("do({\"a\":\"abc\"})", serializeAsString(m, new JSONPObject("do", ob, type)));
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_285_", __ins_v1);
    }
}
