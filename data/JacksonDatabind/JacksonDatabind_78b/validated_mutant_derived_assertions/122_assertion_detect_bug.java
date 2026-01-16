// Instrumented at 2025-12-13 14:00:38
package com.fasterxml.jackson.databind.type;

import java.util.*;
import com.fasterxml.jackson.databind.*;

/**
 * Unit tests for more complicated type definitions where type name
 * aliasing can confuse naive resolution algorithms.
 */
public class TypeAliasesTest extends BaseMapTest {

    public static abstract class Base<T> {

        public T inconsequential = null;
    }

    public static abstract class BaseData<T> {

        public T dataObj;
    }

    public static class Child extends Base<Long> {

        public static class ChildData extends BaseData<List<String>> {
        }
    }

    /*
    /*******************************************************
    /* Unit tests
    /*******************************************************
     */
    // Reproducing [databind#743]
    public void testAliasResolutionIssue743() throws Exception {
        ObjectMapper __ins_v1 = null;
        String s3 = "{\"dataObj\" : [ \"one\", \"two\", \"three\" ] }";
        __ins_v1 = new ObjectMapper();
        ObjectMapper m = __ins_v1;
        Child.ChildData d = m.readValue(s3, Child.ChildData.class);
        assertNotNull(d.dataObj);
        assertEquals(3, d.dataObj.size());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_3239_18", __ins_v1);
    }
}
