// Instrumented at 2025-12-01 00:17:03
package com.fasterxml.jackson.databind.seq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

// for [databind#827]
public class PolyMapWriter827Test extends BaseMapTest {

    static class CustomKey {

        String a;

        int b;

        @Override
        public String toString() {
            return "BAD-KEY";
        }
    }

    public class CustomKeySerializer extends JsonSerializer<CustomKey> {

        @Override
        public void serialize(CustomKey key, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeFieldName(key.a + "," + key.b);
        }
    }

    public void testPolyCustomKeySerializer() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new SimpleModule("keySerializerModule").addKeySerializer(CustomKey.class, new CustomKeySerializer()));
        Map<CustomKey, String> map = new HashMap<CustomKey, String>();
        CustomKey key = new CustomKey();
        key.a = "foo";
        key.b = 1;
        map.put(key, "bar");
        final ObjectWriter writer = mapper.writerFor(new TypeReference<Map<CustomKey, String>>() {
        });
        String json = writer.writeValueAsString(map);
        Assert.assertEquals("[\"java.util.HashMap\",{\"foo,1\":\"bar\"}]", json);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1432_32", __ins_v1);
    }
}
