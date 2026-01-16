// Instrumented at 2025-12-10 19:59:23
package com.fasterxml.jackson.databind.creators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;

/**
 * Tests to ensure that deserialization fails when a bean property has a null value
 * Relates to <a href="https://github.com/FasterXML/jackson-databind/issues/988">issue #988</a>
 */
public class FailOnNullCreatorTest extends BaseMapTest {

    static class Person {

        String name;

        Integer age;

        @JsonCreator
        public Person(@JsonProperty(value = "name") String name, @JsonProperty(value = "age") int age) {
            this.name = name;
            this.age = age;
        }
    }

    private final ObjectReader POINT_READER = objectMapper().readerFor(Person.class);

    public void testRequiredNonNullParam() throws Exception {
        com.fasterxml.jackson.databind.ObjectReader __ins_v1 = null;
        Person p;
        __ins_v1 = POINT_READER;
        // First: fine if feature is not enabled
        p = __ins_v1.readValue(aposToQuotes("{}"));
        assertEquals(null, p.name);
        assertEquals(Integer.valueOf(0), p.age);
        // Second: fine if feature is enabled but default value is not null
        ObjectReader r = POINT_READER.with(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);
        p = POINT_READER.readValue(aposToQuotes("{'name':'John', 'age': null}"));
        assertEquals("John", p.name);
        assertEquals(Integer.valueOf(0), p.age);
        // Third: throws exception if property is missing
        try {
            r.readValue(aposToQuotes("{}"));
            fail("Should not pass third test");
        } catch (JsonMappingException e) {
            verifyException(e, "Null value for creator property 'name'");
        }
        // Fourth: throws exception if property is set to null explicitly
        try {
            r.readValue(aposToQuotes("{'age': 5, 'name': null}"));
            fail("Should not pass fourth test");
        } catch (JsonMappingException e) {
            verifyException(e, "Null value for creator property 'name'");
        }
        org.helper.Assertions.verify("var._context._factory_1851_", __ins_v1);
    }
}
