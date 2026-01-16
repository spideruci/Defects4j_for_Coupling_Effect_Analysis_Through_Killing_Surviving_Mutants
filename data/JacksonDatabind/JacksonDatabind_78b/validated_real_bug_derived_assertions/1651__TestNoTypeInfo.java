// Instrumented at 2025-12-10 19:59:25
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class TestNoTypeInfo extends BaseMapTest {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonDeserialize(as = NoType.class)
    static interface NoTypeInterface {
    }

    final static class NoType implements NoTypeInterface {

        public int a = 3;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    // for [JACKSON-746]
    public void testWithIdNone() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        final ObjectMapper mapper = __ins_v1;
        mapper.enableDefaultTyping();
        // serialize without type info
        String json = mapper.writeValueAsString(new NoType());
        assertEquals("{\"a\":3}", json);
        // and deserialize successfully
        NoTypeInterface bean = mapper.readValue("{\"a\":6}", NoTypeInterface.class);
        assertNotNull(bean);
        NoType impl = (NoType) bean;
        assertEquals(6, impl.a);
        org.helper.Assertions.verify("var._deserializationContext._factory_1651_", __ins_v1);
    }
}
