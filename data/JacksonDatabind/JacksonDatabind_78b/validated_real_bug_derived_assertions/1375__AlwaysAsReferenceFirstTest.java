// Instrumented at 2025-12-10 19:59:26
package com.fasterxml.jackson.databind.objectid;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class AlwaysAsReferenceFirstTest extends BaseMapTest {

    @JsonPropertyOrder({ "bar1", "bar2" })
    static class Foo {

        @JsonIdentityReference(alwaysAsId = true)
        public Bar bar1;

        @JsonIdentityReference
        public Bar bar2;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
    static class Bar {

        public int value = 3;
    }

    public void testIssue1255() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;
        String json = mapper.writeValueAsString(mo);
        Foo result = mapper.readValue(json, Foo.class);
        assertNotNull(result);
        org.helper.Assertions.verify("var._deserializationContext._factory_1375_", __ins_v1);
    }
}
