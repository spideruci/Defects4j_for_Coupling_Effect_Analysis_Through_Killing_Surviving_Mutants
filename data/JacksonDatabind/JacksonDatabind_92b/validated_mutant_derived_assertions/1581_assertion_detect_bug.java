// Instrumented at 2025-12-01 00:17:15
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
        __ins_v1 = new ObjectMapper();
        // first, default settings, with which construction works ok
        ObjectMapper m = __ins_v1;
        PrivateBean bean = m.readValue("\"abc\"", PrivateBean.class);
        assertEquals("abc", bean.a);
        // then by increasing visibility requirement:
        m = new ObjectMapper();
        // note: clumsy code, but needed for Eclipse/JDK1.5 compilation (not for 1.6)
        VisibilityChecker<?> vc = m.getVisibilityChecker();
        vc = vc.withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);
        m.setVisibility(vc);
        try {
            m.readValue("\"abc\"", PrivateBean.class);
            fail("Expected exception for missing constructor");
        } catch (JsonProcessingException e) {
            verifyException(e, "no single-String constructor/factory");
        }
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_237_32", __ins_v1);
    }
}
