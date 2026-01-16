// Instrumented at 2025-12-01 00:17:11
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.*;

public class LocalTypeTest extends BaseMapTest {

    // [databind#609]
    static class EntityContainer {

        RuleForm entity;

        @SuppressWarnings("unchecked")
        public <T extends RuleForm> T getEntity() {
            return (T) entity;
        }

        public <T extends RuleForm> void setEntity(T e) {
            entity = e;
        }
    }

    static class RuleForm {

        public int value;

        public RuleForm() {
        }

        public RuleForm(int v) {
            value = v;
        }
    }

    // [databind#609]
    public void testLocalPartialType609() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        EntityContainer input = new EntityContainer();
        input.entity = new RuleForm(12);
        String json = mapper.writeValueAsString(input);
        EntityContainer output = mapper.readValue(json, EntityContainer.class);
        assertEquals(12, output.getEntity().value);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1276_32", __ins_v1);
    }
}
