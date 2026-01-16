// Instrumented at 2025-12-01 00:17:02
package com.fasterxml.jackson.databind.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.*;

public class JsonIgnoreProperties1595Test extends BaseMapTest {

    @JsonIgnoreProperties(value = { "name" }, allowSetters = true)
    @JsonPropertyOrder(alphabetic = true)
    static class Simple {

        private int id;

        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public void testIgnoreGetterNotSetter1595() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        Simple config = new Simple();
        config.setId(123);
        config.setName("jack");
        String json = mapper.writeValueAsString(config);
        assertEquals(aposToQuotes("{'id':123}"), json);
        Simple des = mapper.readValue(aposToQuotes("{'id':123,'name':'jack'}"), Simple.class);
        assertEquals("jack", des.getName());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_463_32", __ins_v1);
    }
}
