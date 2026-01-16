// Instrumented at 2025-12-10 19:59:29
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import java.util.*;

// [databind#1395]: prevent attempts at including type info for primitives
public class DefaultTypingWithPrimitivesTest extends BaseMapTest {

    static class Data {

        public long key;
    }

    public void testDefaultTypingWithLong() throws Exception {
        ObjectMapper __ins_v1 = null;
        Data data = new Data();
        data.key = 1L;
        Map<String, Object> mapData = new HashMap<String, Object>();
        mapData.put("longInMap", 2L);
        mapData.put("longAsField", data);
        __ins_v1 = new ObjectMapper();
        // Configure Jackson to preserve types
        ObjectMapper mapper = __ins_v1;
        StdTypeResolverBuilder resolver = new StdTypeResolverBuilder();
        resolver.init(JsonTypeInfo.Id.CLASS, null);
        resolver.inclusion(JsonTypeInfo.As.PROPERTY);
        resolver.typeProperty("__t");
        mapper.setDefaultTyping(resolver);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Serialize
        String json = mapper.writeValueAsString(mapData);
        // Deserialize
        Map<?, ?> result = mapper.readValue(json, Map.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        org.helper.Assertions.verify("var._deserializationContext._factory_407_", __ins_v1);
    }
}
