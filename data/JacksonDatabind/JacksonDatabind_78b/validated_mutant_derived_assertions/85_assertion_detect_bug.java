// Instrumented at 2025-12-13 14:00:38
package com.fasterxml.jackson.databind.deser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.fasterxml.jackson.databind.*;

public class TestTimestampDeserialization extends BaseMapTest {

    // As for TestDateDeserialization except we don't need to test date conversion routines, so
    // just check we pick up timestamp class
    public void testTimestampUtil() throws Exception {
        ObjectMapper __ins_v1 = null;
        long now = 123456789L;
        java.sql.Timestamp value = new java.sql.Timestamp(now);
        __ins_v1 = new ObjectMapper();
        // First from long
        assertEquals(value, __ins_v1.readValue("" + now, java.sql.Timestamp.class));
        String dateStr = serializeTimestampAsString(value);
        java.sql.Timestamp result = new ObjectMapper().readValue("\"" + dateStr + "\"", java.sql.Timestamp.class);
        assertEquals("Date: expect " + value + " (" + value.getTime() + "), got " + result + " (" + result.getTime() + ")", value.getTime(), result.getTime());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1818_18", __ins_v1);
    }

    public void testTimestampUtilSingleElementArray() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        long now = System.currentTimeMillis();
        java.sql.Timestamp value = new java.sql.Timestamp(now);
        // First from long
        assertEquals(value, mapper.readValue("[" + now + "]", java.sql.Timestamp.class));
        String dateStr = serializeTimestampAsString(value);
        java.sql.Timestamp result = mapper.readValue("[\"" + dateStr + "\"]", java.sql.Timestamp.class);
        assertEquals("Date: expect " + value + " (" + value.getTime() + "), got " + result + " (" + result.getTime() + ")", value.getTime(), result.getTime());
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    private String serializeTimestampAsString(java.sql.Timestamp value) {
        /* Then from String. This is bit tricky, since JDK does not really
         * suggest a 'standard' format. So let's try using something...
         */
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return df.format(value);
    }
}
