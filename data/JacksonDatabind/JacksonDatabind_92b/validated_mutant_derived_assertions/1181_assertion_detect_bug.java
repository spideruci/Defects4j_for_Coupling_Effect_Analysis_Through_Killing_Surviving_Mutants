// Instrumented at 2025-12-01 00:17:11
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
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        assertVersion(mapper);
        assertVersion(mapper.reader());
        assertVersion(mapper.writer());
        assertVersion(new JacksonAnnotationIntrospector());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1128_32", __ins_v1);
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
