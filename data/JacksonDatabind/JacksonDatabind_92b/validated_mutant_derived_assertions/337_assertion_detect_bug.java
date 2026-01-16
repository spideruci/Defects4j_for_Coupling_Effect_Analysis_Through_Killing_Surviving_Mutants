// Instrumented at 2025-12-01 00:17:00
package com.fasterxml.jackson.databind.convert;

import com.fasterxml.jackson.databind.*;

public class NumericConversionTest extends BaseMapTest {

    private final ObjectMapper MAPPER = objectMapper();

    private final ObjectReader R = MAPPER.reader().without(DeserializationFeature.ACCEPT_FLOAT_AS_INT);

    public void testDoubleToInt() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = MAPPER;
        // by default, should be ok
        Integer I = __ins_v1.readValue(" 1.25 ", Integer.class);
        assertEquals(1, I.intValue());
        IntWrapper w = MAPPER.readValue("{\"i\":-2.25 }", IntWrapper.class);
        assertEquals(-2, w.i);
        try {
            R.forType(Integer.class).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
        try {
            R.forType(Integer.TYPE).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
        try {
            R.forType(IntWrapper.class).readValue("{\"i\":-2.25 }");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_517_32", __ins_v1);
    }

    public void testDoubleToLong() throws Exception {
        // by default, should be ok
        Long L = MAPPER.readValue(" 3.33 ", Long.class);
        assertEquals(3L, L.longValue());
        LongWrapper w = MAPPER.readValue("{\"l\":-2.25 }", LongWrapper.class);
        assertEquals(-2L, w.l);
        try {
            R.forType(Long.class).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
        try {
            R.forType(Long.TYPE).readValue("1.5");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
        try {
            R.forType(LongWrapper.class).readValue("{\"l\": 7.7 }");
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Can not coerce a floating-point");
        }
    }
}
