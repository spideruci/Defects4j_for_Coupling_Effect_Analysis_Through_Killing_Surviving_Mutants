// Instrumented at 2025-12-07 08:48:58
package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.JsonParserSequence;

@SuppressWarnings("resource")
public class ParserSequenceTest extends com.fasterxml.jackson.core.BaseTest {

    public void testSimple() throws Exception {
        com.fasterxml.jackson.core.util.JsonParserSequence __ins_v1 = null;
        JsonParser p1 = JSON_FACTORY.createParser("[ 1 ]");
        JsonParser p2 = JSON_FACTORY.createParser("[ 2 ]");
        __ins_v1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq = __ins_v1;
        assertEquals(2, seq.containedParsersCount());
        assertFalse(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertToken(JsonToken.START_ARRAY, seq.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());
        assertFalse(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertToken(JsonToken.START_ARRAY, seq.nextToken());
        // first parser ought to be closed now
        assertTrue(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());
        assertTrue(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertNull(seq.nextToken());
        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(seq.isClosed());
        seq.close();
        org.helper.Assertions.verify("var._parsers.elements._currInputProcessed_4_6", __ins_v1);
    }

    // for [jackson-core#296]
    public void testInitialized() throws Exception {
        JsonParser p1 = JSON_FACTORY.createParser("1 2");
        JsonParser p2 = JSON_FACTORY.createParser("3 false");
        // consume '1', move to '2'
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        seq.close();
    }
}
