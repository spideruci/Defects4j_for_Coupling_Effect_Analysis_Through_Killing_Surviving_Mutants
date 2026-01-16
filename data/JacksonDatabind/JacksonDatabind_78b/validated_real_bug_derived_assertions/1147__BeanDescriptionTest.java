// Instrumented at 2025-12-10 19:59:23
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.databind.*;

public class BeanDescriptionTest extends BaseMapTest {

    private final ObjectMapper MAPPER = objectMapper();

    private final static String CLASS_DESC = "Description, yay!";

    @JsonClassDescription(CLASS_DESC)
    static class DocumentedBean {

        public int x;
    }

    public void testClassDesc() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = MAPPER;
        BeanDescription beanDesc = MAPPER.getDeserializationConfig().introspect(__ins_v1.constructType(DocumentedBean.class));
        assertEquals(CLASS_DESC, beanDesc.findClassDescription());
        org.helper.Assertions.verify("var._deserializationContext._factory_1147_", __ins_v1);
    }
}
