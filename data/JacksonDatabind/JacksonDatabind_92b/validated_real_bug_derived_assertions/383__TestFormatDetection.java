// Instrumented at 2025-11-28 09:38:31
package com.fasterxml.jackson.databind.interop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

public class TestFormatDetection extends BaseMapTest {

    private final ObjectReader READER = objectReader();

    static class POJO {

        public int x, y;

        public POJO() {
        }

        public POJO(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    public void testSimpleWithJSON() throws Exception {
        com.fasterxml.jackson.databind.ObjectReader __ins_v1 = null;
        ObjectReader detecting = READER.forType(POJO.class);
        __ins_v1 = detecting.withFormatDetection(detecting);
        detecting = __ins_v1;
        POJO pojo = detecting.readValue(utf8Bytes("{\"x\":1}"));
        assertNotNull(pojo);
        assertEquals(1, pojo.x);
        org.helper.Assertions.verify("var._context._factory.DEFAULT_NO_DESER_CLASS_NAMES_383_", __ins_v1);
    }

    public void testInvalid() throws Exception {
        ObjectReader detecting = READER.forType(POJO.class);
        detecting = detecting.withFormatDetection(detecting);
        try {
            detecting.readValue(utf8Bytes("<POJO><x>1</x></POJO>"));
            fail("Should have failed");
        } catch (JsonProcessingException e) {
            verifyException(e, "Can not detect format from input");
        }
    }
}
