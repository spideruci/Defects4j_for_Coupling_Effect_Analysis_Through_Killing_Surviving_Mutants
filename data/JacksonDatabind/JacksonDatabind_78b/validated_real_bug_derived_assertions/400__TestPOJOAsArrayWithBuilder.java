// Instrumented at 2025-12-10 19:59:24
package com.fasterxml.jackson.databind.struct;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Unit tests for "POJO as array" feature using Builder-style
 * POJO construction.
 */
public class TestPOJOAsArrayWithBuilder extends BaseMapTest {

    @JsonDeserialize(builder = SimpleBuilderXY.class)
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(alphabetic = true)
    static class ValueClassXY {

        final int _x, _y;

        protected ValueClassXY(int x, int y) {
            _x = x + 1;
            _y = y + 1;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class SimpleBuilderXY {

        public int x, y;

        public SimpleBuilderXY withX(int x0) {
            this.x = x0;
            return this;
        }

        public SimpleBuilderXY withY(int y0) {
            this.y = y0;
            return this;
        }

        public ValueClassXY build() {
            return new ValueClassXY(x, y);
        }
    }

    /*
    /*****************************************************
    /* Basic tests
    /*****************************************************
     */
    private final static ObjectMapper MAPPER = new ObjectMapper();

    public void testSimpleBuilder() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        __ins_v1 = MAPPER;
        // Ok, first, ensure that serializer will "black out" filtered properties
        ValueClassXY value = __ins_v1.readValue("[1,2]", ValueClassXY.class);
        assertEquals(2, value._x);
        assertEquals(3, value._y);
        org.helper.Assertions.verify("var._deserializationContext._factory_400_", __ins_v1);
    }
}
