// Instrumented at 2025-12-01 00:17:09
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.cfg.PackageVersion;

/**
 * Tests to ensure that we get proper Version information via
 * things defined as Versioned.
 */
public class TestVersions extends BaseMapTest {

    public void testMapperVersions() {
        com.fasterxml.jackson.databind.ObjectReader __ins_v1 = null;
        ObjectMapper mapper = new ObjectMapper();
        assertVersion(mapper);
        __ins_v1 = mapper.reader();
        assertVersion(__ins_v1);
        assertVersion(mapper.writer());
        assertVersion(new JacksonAnnotationIntrospector());
        org.helper.Assertions.verify("var._context._factory.DEFAULT_NO_DESER_CLASS_NAMES_1129_32", __ins_v1);
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    private void assertVersion(Versioned vers) {
        Version v = vers.version();
        assertFalse("Should find version information (got " + v + ")", v.isUnknownVersion());
        Version exp = PackageVersion.VERSION;
        assertEquals(exp.toFullString(), v.toFullString());
        assertEquals(exp, v);
    }
}
