// Instrumented at 2025-12-10 19:59:25
package com.fasterxml.jackson.databind.deser.exc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class TestExceptionHandlingWithJsonCreatorDeserialization extends BaseMapTest {

    static class Foo {

        private Bar bar;

        @JsonCreator
        public Foo(@JsonProperty("bar") Bar bar) {
            this.bar = bar;
        }

        public Bar getBar() {
            return bar;
        }
    }

    static class Bar {

        private Baz baz;

        @JsonCreator
        public Bar(@JsonProperty("baz") Baz baz) {
            this.baz = baz;
        }

        public Baz getBaz() {
            return baz;
        }
    }

    static class Baz {

        private String qux;

        @JsonCreator
        public Baz(@JsonProperty("qux") String qux) {
            this.qux = qux;
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
        final String THIS = getClass().getName();
        // when
        try {
            mapper.readValue(input, Foo.class);
            fail("Upsss! Exception has not been thrown.");
        } catch (JsonMappingException ex) {
            // then
            assertEquals(THIS + "$Foo[\"bar\"]->" + THIS + "$Bar[\"baz\"]", ex.getPathReference());
        }
        org.helper.Assertions.verify("var._deserializationContext._factory_401_", __ins_v1);
    }
}
