// Instrumented at 2025-11-28 09:38:29
package com.fasterxml.jackson.databind.creators;

import java.beans.ConstructorProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;

// for [databind#1367]
public class CreatorWithObjectIdTest extends BaseMapTest {

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class //            resolver = SimpleObjectIdResolver.class)
    A {

        String id;

        String name;

        public A() {
        }

        @ConstructorProperties({ "id", "name" })
        public A(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public void testObjectIdWithCreator() throws Exception {
        ObjectMapper __ins_v1 = null;
        A a = new A("123", "A");
        __ins_v1 = new ObjectMapper();
        ObjectMapper om = __ins_v1;
        String json = om.writeValueAsString(a);
        A deser = om.readValue(json, A.class);
        assertEquals(a.name, deser.name);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_130_", __ins_v1);
    }
}
