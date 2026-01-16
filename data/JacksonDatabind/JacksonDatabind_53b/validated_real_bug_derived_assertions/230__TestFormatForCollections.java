// Instrumented at 2025-12-10 23:03:39
package com.fasterxml.jackson.databind.struct;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.*;

public class TestFormatForCollections extends BaseMapTest {

    // [issue#40]: Allow serialization 'as POJO' (resulting in JSON Object)
    @JsonPropertyOrder({ "size", "value" })
    @JsonFormat(shape = Shape.OBJECT)
    // from 'isEmpty()'
    @JsonIgnoreProperties({ "empty" })
    static class CollectionAsPOJO extends ArrayList<String> {

        private static final long serialVersionUID = 1L;

        @JsonProperty("size")
        public int foo() {
            return size();
        }

        public List<String> getValues() {
            return new ArrayList<String>(this);
        }

        public void setValues(List<String> v) {
            addAll(v);
        }

        // bogus setter to handle "size" property
        public void setSize(int i) {
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    private final static ObjectMapper MAPPER = new ObjectMapper();

    // [Issue#40]
    public void testListAsObject() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        // First, serialize a "POJO-List"
        CollectionAsPOJO list = new CollectionAsPOJO();
        list.add("a");
        list.add("b");
        __ins_v1 = MAPPER;
        String json = __ins_v1.writeValueAsString(list);
        assertEquals("{\"size\":2,\"values\":[\"a\",\"b\"]}", json);
        // and then bring it back!
        CollectionAsPOJO result = MAPPER.readValue(json, CollectionAsPOJO.class);
        assertEquals(2, result.size());
        org.helper.Assertions.verify("var._rootDeserializers_230_", __ins_v1);
    }
}
