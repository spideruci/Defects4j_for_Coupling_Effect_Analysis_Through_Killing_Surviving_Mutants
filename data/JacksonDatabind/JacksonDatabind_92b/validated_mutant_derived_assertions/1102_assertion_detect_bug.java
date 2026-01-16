// Instrumented at 2025-12-01 00:17:10
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;

public class TestInnerClass extends BaseMapTest {

    // [JACKSON-594]
    static class Dog {

        public String name;

        public Brain brain;

        public Dog() {
        }

        public Dog(String n, boolean thinking) {
            name = n;
            brain = new Brain();
            brain.isThinking = thinking;
        }

        // note: non-static
        public class Brain {

            public boolean isThinking;

            public String parentName() {
                return name;
            }
        }
    }

    /*
    /**********************************************************
    /* Tests
    /**********************************************************
     */
    public void testSimpleNonStaticInner() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        // Let's actually verify by first serializing, then deserializing back
        ObjectMapper mapper = __ins_v1;
        Dog input = new Dog("Smurf", true);
        String json = mapper.writeValueAsString(input);
        Dog output = mapper.readValue(json, Dog.class);
        assertEquals("Smurf", output.name);
        assertNotNull(output.brain);
        assertTrue(output.brain.isThinking);
        // and verify correct binding...
        assertEquals("Smurf", output.brain.parentName());
        output.name = "Foo";
        assertEquals("Foo", output.brain.parentName());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_745_32", __ins_v1);
    }
}
