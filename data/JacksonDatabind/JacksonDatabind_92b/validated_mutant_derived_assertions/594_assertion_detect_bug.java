// Instrumented at 2025-12-01 00:17:03
package com.fasterxml.jackson.databind.jsonschema;

import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;

/**
 * Trivial test to ensure <code>JsonSchema</code> can be also deserialized
 */
@SuppressWarnings("deprecation")
public class TestReadJsonSchema extends com.fasterxml.jackson.databind.BaseMapTest {

    enum SchemaEnum {

        YES, NO
    }

    static class Schemable {

        public String name;

        public char[] nameBuffer;

        // We'll include tons of stuff, just to force generation of schema
        public boolean[] states;

        public byte[] binaryData;

        public short[] shorts;

        public int[] ints;

        public long[] longs;

        public float[] floats;

        public double[] doubles;

        public Object[] objects;

        public JsonSerializable someSerializable;

        public Iterable<Object> iterableOhYeahBaby;

        public List<String> extra;

        public ArrayList<String> extra2;

        public Iterator<String[]> extra3;

        public Map<String, Double> sizes;

        public EnumMap<SchemaEnum, List<String>> whatever;

        SchemaEnum testEnum;

        public EnumSet<SchemaEnum> testEnums;
    }

    /**
     * Verifies that a simple schema that is serialized can be
     * deserialized back to equal schema instance
     */
    public void testDeserializeSimple() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        JsonSchema schema = mapper.generateJsonSchema(Schemable.class);
        assertNotNull(schema);
        String schemaStr = mapper.writeValueAsString(schema);
        assertNotNull(schemaStr);
        JsonSchema result = mapper.readValue(schemaStr, JsonSchema.class);
        assertEquals("Trying to read from '" + schemaStr + "'", schema, result);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1776_32", __ins_v1);
    }
}
