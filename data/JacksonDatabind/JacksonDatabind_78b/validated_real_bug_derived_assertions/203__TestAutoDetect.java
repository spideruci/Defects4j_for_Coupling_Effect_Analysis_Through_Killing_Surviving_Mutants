// Instrumented at 2025-12-10 19:59:20
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

public class TestAutoDetect extends BaseMapTest {

    static class PrivateBean {

        String a;

        private PrivateBean() {
        }

        private PrivateBean(String a) {
            this.a = a;
        }
    }

    /*
    /********************************************************
    /* Unit tests
    /********************************************************
     */
    public void testPrivateCtor() throws Exception {
        ObjectMapper __ins_v1 = null;
        // first, default settings, with which construction works ok
        ObjectMapper m = new ObjectMapper();
        PrivateBean bean = m.readValue("\"abc\"", PrivateBean.class);
        assertEquals("abc", bean.a);
        __ins_v1 = new ObjectMapper();
        // then by increasing visibility requirement:
        m = __ins_v1;
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);
        m.setVisibility(vc);
        try {
            m.readValue("\"abc\"", PrivateBean.class);
            fail("Expected exception for missing constructor");
        } catch (JsonProcessingException e) {
            verifyException(e, "no String-argument constructor/factory");
        }
        org.helper.Assertions.verify("var._deserializationContext._factory_203_", __ins_v1);
    }
}
