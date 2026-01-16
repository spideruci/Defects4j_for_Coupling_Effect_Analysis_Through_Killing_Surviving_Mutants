// Instrumented at 2025-12-01 00:16:56
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class TestExceptionHandlingWithDefaultDeserialization extends BaseMapTest {

    static class Foo {

        private Bar bar;

        public Foo() {
        }

        public Bar getBar() {
            return bar;
        }
    }

    static class Bar {

        private Baz baz;

        public Bar() {
        }

        public Baz getBaz() {
            return baz;
        }
    }

    static class Baz {

        private String qux;

        public Baz() {
        }

        public String getQux() {
            return qux;
        }
    }

    public void testShouldThrowJsonMappingExceptionWithPathReference() throws IOException {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        // given
        ObjectMapper mapper = __ins_v1;
        String input = "{\"bar\":{\"baz\":{qux:\"quxValue\"))}";
        // when
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            // then
            assertEquals("com.fasterxml.jackson.databind.deser.Foo[\"bar\"]->com.fasterxml.jackson.databind.deser.Bar[\"baz\"]", ex.getPathReference());
        }
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1224_32", __ins_v1);
    }
}
