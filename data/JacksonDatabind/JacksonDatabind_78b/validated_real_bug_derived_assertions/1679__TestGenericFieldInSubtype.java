// Instrumented at 2025-12-10 19:59:18
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.*;

public class TestGenericFieldInSubtype extends BaseMapTest {

    // [JACKSON-677]
    public void test677() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        // and bit more checking as per later comments
        JavaType t677 = mapper.constructType(Result677.Success677.class);
        assertNotNull(t677);
        Result677.Success677<Integer> s = new Result677.Success677<Integer>(Integer.valueOf(4));
        String json = mapper.writeValueAsString(s);
        assertEquals("{\"value\":4}", json);
        org.helper.Assertions.verify("var._deserializationContext._factory_1679_", __ins_v1);
    }

    // [JACKSON-887]
    public void testInnerType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BaseType.SubType<?> r = mapper.readValue("{}", BaseType.SubType.class);
        assertNotNull(r);
    }
}

class Result677<T> {

    public static class Success677<K> extends Result677<K> {

        public K value;

        public Success677() {
        }

        public Success677(K k) {
            value = k;
        }
    }
}

abstract class BaseType<T> {

    public T value;

    public final static class SubType<T extends Number> extends BaseType<T> {
    }
}
