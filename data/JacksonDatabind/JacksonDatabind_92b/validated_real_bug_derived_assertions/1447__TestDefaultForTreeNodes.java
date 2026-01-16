// Instrumented at 2025-11-28 09:38:32
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;

public class TestDefaultForTreeNodes extends BaseMapTest {

    public static class Foo {

        public String bar;

        public Foo() {
        }

        public Foo(String b) {
            bar = b;
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    private final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    {
        DEFAULT_MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    public void testValueAsStringWithDefaultTyping() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        Foo foo = new Foo("baz");
        __ins_v1 = DEFAULT_MAPPER;
        String json = __ins_v1.writeValueAsString(foo);
        JsonNode jsonNode = DEFAULT_MAPPER.readTree(json);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1447_", __ins_v1);
    }

    public void testValueToTreeWithDefaultTyping() throws Exception {
        Foo foo = new Foo("baz");
        JsonNode jsonNode = DEFAULT_MAPPER.valueToTree(foo);
        assertEquals(jsonNode.get("bar").textValue(), foo.bar);
    }
}
