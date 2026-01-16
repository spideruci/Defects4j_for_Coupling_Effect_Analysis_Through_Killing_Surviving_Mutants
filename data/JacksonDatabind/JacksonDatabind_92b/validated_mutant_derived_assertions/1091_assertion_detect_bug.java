// Instrumented at 2025-12-01 00:17:10
package com.fasterxml.jackson.databind.objectid;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class ObjectId825Test extends BaseMapTest {

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "oidString")
    public static class AbstractEntity {

        public String oidString;
    }

    public static class TestA extends AbstractEntity {

        public TestAbst testAbst;

        public TestD d;
    }

    static class TestAbst extends AbstractEntity {
    }

    static class TestC extends TestAbst {

        public TestD d;
    }

    static class TestD extends AbstractEntity {
    }

    private final ObjectMapper DEF_TYPING_MAPPER = new ObjectMapper();

    {
        DEF_TYPING_MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public void testDeserialize() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        TestA a = new TestA();
        a.oidString = "oidA";
        TestC c = new TestC();
        c.oidString = "oidC";
        a.testAbst = c;
        TestD d = new TestD();
        d.oidString = "oidD";
        c.d = d;
        a.d = d;
        __ins_v1 = DEF_TYPING_MAPPER;
        String json = __ins_v1.writeValueAsString(a);
        //        System.out.println("JSON: " + json);
        TestA testADeserialized = DEF_TYPING_MAPPER.readValue(json, TestA.class);
        assertNotNull(testADeserialized);
        assertNotNull(testADeserialized.d);
        assertEquals("oidD", testADeserialized.d.oidString);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1470_32", __ins_v1);
    }
}
