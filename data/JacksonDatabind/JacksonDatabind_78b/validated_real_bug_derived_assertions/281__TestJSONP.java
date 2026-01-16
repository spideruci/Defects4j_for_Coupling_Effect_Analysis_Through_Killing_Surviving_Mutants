// Instrumented at 2025-12-10 19:59:19
package com.fasterxml.jackson.databind.misc;

import java.util.Arrays;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;

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

    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testSimpleScalars() throws Exception {
        assertEquals("callback(\"abc\")", MAPPER.writeValueAsString(new JSONPObject("callback", "abc")));
        assertEquals("calc(123)", MAPPER.writeValueAsString(new JSONPObject("calc", Integer.valueOf(123))));
        assertEquals("dummy(null)", MAPPER.writeValueAsString(new JSONPObject("dummy", null)));
    }

    public void testSimpleBean() throws Exception {
        assertEquals("xxx({\"a\":\"123\",\"b\":\"456\"})", MAPPER.writeValueAsString(new JSONPObject("xxx", new Impl("123", "456"))));
    }

    /**
     * Test to ensure that it is possible to force a static type for wrapped
     * value.
     */
    public void testWithType() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        Object ob = new Impl("abc", "def");
        __ins_v1 = MAPPER;
        JavaType type = __ins_v1.constructType(Base.class);
        assertEquals("do({\"a\":\"abc\"})", MAPPER.writeValueAsString(new JSONPObject("do", ob, type)));
        org.helper.Assertions.verify("var._deserializationContext._factory_281_", __ins_v1);
    }

    public void testGeneralWrapping() throws Exception {
        JSONWrappedObject input = new JSONWrappedObject("/*Foo*/", "\n// the end", Arrays.asList());
        assertEquals("/*Foo*/[]\n// the end", MAPPER.writeValueAsString(input));
    }
}
