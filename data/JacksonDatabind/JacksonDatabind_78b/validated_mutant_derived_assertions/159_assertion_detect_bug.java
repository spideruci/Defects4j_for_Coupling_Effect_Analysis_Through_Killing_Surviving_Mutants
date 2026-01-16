// Instrumented at 2025-12-13 14:00:39
package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TestJsonSerialize3 extends BaseMapTest {

    // [JACKSON-829]
    static class FooToBarSerializer extends JsonSerializer<String> {

        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if ("foo".equals(value)) {
                jgen.writeString("bar");
            } else {
                jgen.writeString(value);
            }
        }
    }

    static class MyObject {

        @JsonSerialize(contentUsing = FooToBarSerializer.class)
        List<String> list;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    public void testCustomContentSerializer() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper m = __ins_v1;
        MyObject object = new MyObject();
        object.list = Arrays.asList("foo");
        String json = m.writeValueAsString(object);
        assertEquals("{\"list\":[\"bar\"]}", json);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_2039_18", __ins_v1);
    }
}
