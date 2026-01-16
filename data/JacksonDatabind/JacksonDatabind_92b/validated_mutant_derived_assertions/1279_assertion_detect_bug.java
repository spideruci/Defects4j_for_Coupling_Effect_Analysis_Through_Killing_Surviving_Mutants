// Instrumented at 2025-12-01 00:17:12
package com.fasterxml.jackson.databind.objectid;

import java.util.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class Objecid1083Test extends BaseMapTest {

    public static class JsonRoot {

        public final List<JsonSchema> schemas = new ArrayList<JsonSchema>();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = JsonMapSchema.class)
    @JsonSubTypes({ @JsonSubTypes.Type(value = JsonMapSchema.class, name = "map"), @JsonSubTypes.Type(value = JsonJdbcSchema.class, name = "jdbc") })
    public static abstract class JsonSchema {

        public String name;
    }

    static class JsonMapSchema extends JsonSchema {
    }

    static class JsonJdbcSchema extends JsonSchema {
    }

    /*
      /*****************************************************
      /* Unit tests, external id deserialization
      /*****************************************************
       */
    public void testSimple() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        final ObjectMapper mapper = __ins_v1;
        final String json = aposToQuotes("{'schemas': [{\n" + "  'name': 'FoodMart'\n" + "}]}\n");
        mapper.readValue(json, JsonRoot.class);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1621_32", __ins_v1);
    }
}
