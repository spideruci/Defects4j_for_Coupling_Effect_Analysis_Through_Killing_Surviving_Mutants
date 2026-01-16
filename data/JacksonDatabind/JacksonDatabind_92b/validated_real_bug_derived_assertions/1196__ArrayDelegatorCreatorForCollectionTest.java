// Instrumented at 2025-11-28 09:38:36
package com.fasterxml.jackson.databind.creators;

import java.util.Collections;
import java.util.Set;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class ArrayDelegatorCreatorForCollectionTest extends BaseMapTest {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    abstract static class UnmodifiableSetMixin {

        @JsonCreator
        public UnmodifiableSetMixin(Set<?> s) {
        }
    }

    public void testUnmodifiable() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        Class<?> unmodSetType = Collections.unmodifiableSet(Collections.<String>emptySet()).getClass();
        mapper.addMixIn(unmodSetType, UnmodifiableSetMixin.class);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        final String EXPECTED_JSON = "[\"" + unmodSetType.getName() + "\",[]]";
        Set<?> foo = mapper.readValue(EXPECTED_JSON, Set.class);
        assertTrue(foo.isEmpty());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1196_", __ins_v1);
    }
}
