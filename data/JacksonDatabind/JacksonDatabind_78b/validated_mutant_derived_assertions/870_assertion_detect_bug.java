// Instrumented at 2025-12-13 14:00:45
package com.fasterxml.jackson.databind.objectid;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class TestObjectIdWithInjectables538 extends BaseMapTest {

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
    public static class A {

        public B b;

        public A(@JacksonInject("i1") String injected) {
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
    public static class B {

        public A a;

        @JsonCreator
        public B(@JacksonInject("i2") String injected) {
        }
    }

    /*
    /*****************************************************
    /* Test methods
    /*****************************************************
     */
    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testWithInjectables538() throws Exception {
        com.fasterxml.jackson.databind.ObjectReader __ins_v1 = null;
        A a = new A("a");
        B b = new B("b");
        a.b = b;
        b.a = a;
        String json = MAPPER.writeValueAsString(a);
        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue("i1", "e1");
        inject.addValue("i2", "e2");
        A output = null;
        try {
            __ins_v1 = MAPPER.reader(inject).forType(A.class);
            output = __ins_v1.readValue(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize from JSON '" + json + "'", e);
        }
        assertNotNull(output);
        assertNotNull(output.b);
        assertSame(output, output.b.a);
        org.helper.Assertions.verify("var._context._factory.DEFAULT_NO_DESER_CLASS_NAMES_3603_18", __ins_v1);
    }
}
