// Instrumented at 2025-11-28 09:38:36
package com.fasterxml.jackson.databind.convert;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.util.StdConverter;

// for [databind#795]
public class ConvertingAbstractSerializer795Test extends BaseMapTest {

    public static abstract class AbstractCustomType {

        final String value;

        public AbstractCustomType(String v) {
            this.value = v;
        }
    }

    public static class ConcreteCustomType extends AbstractCustomType {

        public ConcreteCustomType(String v) {
            super(v);
        }
    }

    public static class AbstractCustomTypeDeserializationConverter extends StdConverter<String, AbstractCustomType> {

        @Override
        public AbstractCustomType convert(String arg) {
            return new ConcreteCustomType(arg);
        }
    }

    public static class AbstractCustomTypeUser {

        @JsonProperty
        @JsonDeserialize(converter = AbstractCustomTypeDeserializationConverter.class)
        private final AbstractCustomType customField;

        @JsonCreator
        AbstractCustomTypeUser(@JsonProperty("customField") AbstractCustomType customField) {
            this.customField = customField;
        }
    }

    public static class NonAbstractCustomType {

        final String value;

        public NonAbstractCustomType(String v) {
            this.value = v;
        }
    }

    public static class NonAbstractCustomTypeDeserializationConverter extends StdConverter<String, NonAbstractCustomType> {

        @Override
        public NonAbstractCustomType convert(String arg) {
            return new NonAbstractCustomType(arg);
        }
    }

    public static class NonAbstractCustomTypeUser {

        @JsonProperty
        @JsonDeserialize(converter = NonAbstractCustomTypeDeserializationConverter.class)
        private final NonAbstractCustomType customField;

        @JsonCreator
        NonAbstractCustomTypeUser(@JsonProperty("customField") NonAbstractCustomType customField) {
            this.customField = customField;
        }
    }

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public void testAbstractTypeDeserialization() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        String test = "{\"customField\": \"customString\"}";
        __ins_v1 = JSON_MAPPER;
        AbstractCustomTypeUser cu = __ins_v1.readValue(test, AbstractCustomTypeUser.class);
        assertNotNull(cu);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1178_", __ins_v1);
    }

    public void testNonAbstractDeserialization() throws Exception {
        String test = "{\"customField\": \"customString\"}";
        NonAbstractCustomTypeUser cu = JSON_MAPPER.readValue(test, NonAbstractCustomTypeUser.class);
        assertNotNull(cu);
    }
}
