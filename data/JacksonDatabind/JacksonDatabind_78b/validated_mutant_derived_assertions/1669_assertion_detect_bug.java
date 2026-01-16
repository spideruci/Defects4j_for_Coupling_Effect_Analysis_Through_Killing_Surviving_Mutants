// Instrumented at 2025-12-13 14:00:51
package com.fasterxml.jackson.databind.jsontype;

import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestDefaultForEnums extends BaseMapTest {

    public enum TestEnum {

        A, B
    }

    static final class EnumHolder {

        // "untyped"
        public Object value;

        public EnumHolder() {
        }

        public EnumHolder(TestEnum e) {
            value = e;
        }
    }

    protected static class TimeUnitBean {

        public TimeUnit timeUnit;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    public void testSimpleEnumBean() throws Exception {
        ObjectMapper __ins_v1 = null;
        TimeUnitBean bean = new TimeUnitBean();
        bean.timeUnit = TimeUnit.SECONDS;
        __ins_v1 = new ObjectMapper();
        // First, without type info
        ObjectMapper m = __ins_v1;
        String json = m.writeValueAsString(bean);
        TimeUnitBean result = m.readValue(json, TimeUnitBean.class);
        assertEquals(TimeUnit.SECONDS, result.timeUnit);
        // then with type info
        m = new ObjectMapper();
        m.enableDefaultTyping();
        json = m.writeValueAsString(bean);
        result = m.readValue(json, TimeUnitBean.class);
        assertEquals(TimeUnit.SECONDS, result.timeUnit);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_2493_18", __ins_v1);
    }

    public void testSimpleEnumsInObjectArray() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        // Typing is needed for enums
        String json = m.writeValueAsString(new Object[] { TestEnum.A });
        assertEquals("[[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"A\"]]", json);
        // and let's verify we get it back ok as well:
        Object[] value = m.readValue(json, Object[].class);
        assertEquals(1, value.length);
        assertSame(TestEnum.A, value[0]);
    }

    public void testSimpleEnumsAsField() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.enableDefaultTyping();
        String json = m.writeValueAsString(new EnumHolder(TestEnum.B));
        assertEquals("{\"value\":[\"com.fasterxml.jackson.databind.jsontype.TestDefaultForEnums$TestEnum\",\"B\"]}", json);
        EnumHolder holder = m.readValue(json, EnumHolder.class);
        assertSame(TestEnum.B, holder.value);
    }
}
