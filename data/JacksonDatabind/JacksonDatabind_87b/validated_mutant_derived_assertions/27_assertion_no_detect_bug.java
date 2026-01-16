// Instrumented at 2026-01-02 00:34:10
package com.fasterxml.jackson.databind;

import java.text.ParseException;
import java.util.*;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class TestStdDateFormat extends BaseMapTest {

    public void testFactories() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.US;
        assertNotNull(StdDateFormat.getISO8601Format(tz, loc));
        assertNotNull(StdDateFormat.getRFC1123Format(tz, loc));
    }

    // [databind#803
    public void testLenient() throws Exception {
        com.fasterxml.jackson.databind.util.StdDateFormat __ins_v1 = null;
        StdDateFormat f = StdDateFormat.instance;
        // default should be lenient
        assertTrue(f.isLenient());
        __ins_v1 = f.clone();
        StdDateFormat f2 = __ins_v1;
        assertTrue(f2.isLenient());
        f2.setLenient(false);
        assertFalse(f2.isLenient());
        f2.setLenient(true);
        assertTrue(f2.isLenient());
        // and for testing, finally, leave as non-lenient
        f2.setLenient(false);
        assertFalse(f2.isLenient());
        StdDateFormat f3 = f2.clone();
        assertFalse(f3.isLenient());
        // first, legal dates are... legal
        Date dt = f3.parse("2015-11-30");
        assertNotNull(dt);
        // but as importantly, when not lenient, do not allow
        try {
            f3.parse("2015-11-32");
            fail("Should not pass");
        } catch (ParseException e) {
            verifyException(e, "can not parse date");
        }
        // ... yet, with lenient, do allow
        f3.setLenient(true);
        dt = f3.parse("2015-11-32");
        assertNotNull(dt);
        org.helper.Assertions.verify("var.DEFAULT_TIMEZONE.name_251_1", __ins_v1);
    }

    public void testInvalid() {
        StdDateFormat std = new StdDateFormat();
        try {
            std.parse("foobar");
        } catch (java.text.ParseException e) {
            verifyException(e, "Can not parse");
        }
    }
}
