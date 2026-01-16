// Instrumented at 2025-12-01 00:17:10
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
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1077_32", __ins_v1);
    }
}
