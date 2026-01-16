// Instrumented at 2025-11-28 09:38:30
package com.fasterxml.jackson.databind.creators;

import java.io.IOException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestCreatorsWithIdentity extends BaseMapTest {

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Parent.class)
    public static class Parent {

        @JsonProperty("id")
        String id;

        @JsonProperty
        String parentProp;

        @JsonCreator
        public Parent(@JsonProperty("parentProp") String parentProp) {
            this.parentProp = parentProp;
        }
    }

    public static class Child {

        @JsonProperty
        Parent parent;

        @JsonProperty
        String childProp;

        @JsonCreator
        public Child(@JsonProperty("parent") Parent parent, @JsonProperty("childProp") String childProp) {
            this.parent = parent;
            this.childProp = childProp;
        }
    }

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public void testSimple() throws IOException {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        String parentStr = "{\"id\" : \"1\", \"parentProp\" : \"parent\"}";
        String childStr = "{\"childProp\" : \"child\", \"parent\" : " + parentStr + "}";
        __ins_v1 = JSON_MAPPER;
        Parent parent = __ins_v1.readValue(parentStr, Parent.class);
        assertNotNull(parent);
        Child child = JSON_MAPPER.readValue(childStr, Child.class);
        assertNotNull(child);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_530_", __ins_v1);
    }
}
