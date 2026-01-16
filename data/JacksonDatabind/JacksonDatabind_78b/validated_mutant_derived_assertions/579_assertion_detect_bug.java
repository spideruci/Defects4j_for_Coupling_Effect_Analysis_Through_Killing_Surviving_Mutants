// Instrumented at 2025-12-13 14:00:42
package com.fasterxml.jackson.databind.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.*;

/**
 * Failing test related to [databind#95]
 */
public class ReadOnlyProperties95Test extends BaseMapTest {

    @JsonIgnoreProperties(value = { "computed" }, allowGetters = true)
    static class ReadOnlyBean {

        public int value = 3;

        public int getComputed() {
            return 32;
        }
    }

    public void testReadOnlyProp() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper m = __ins_v1;
        String json = m.writeValueAsString(new ReadOnlyBean());
        if (json.indexOf("computed") < 0) {
            fail("Should have property 'computed', didn't: " + json);
        }
        ReadOnlyBean bean = m.readValue(json, ReadOnlyBean.class);
        assertNotNull(bean);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_2117_18", __ins_v1);
    }
}
