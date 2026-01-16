// Instrumented at 2025-12-21 13:37:10
package com.fasterxml.jackson.databind.ser;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/**
 * Unit tests for those Jackson types we want to ensure can be serialized.
 */
public class TestJacksonTypes extends BaseMapTest {

    public void testLocation() throws IOException {
        File f = new File("/tmp/test.json");
        JsonLocation loc = new JsonLocation(f, -1, 100, 13);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = writeAndMap(mapper, loc);
        assertEquals(5, result.size());
        assertEquals(f.getAbsolutePath(), result.get("sourceRef"));
        assertEquals(Integer.valueOf(-1), result.get("charOffset"));
        assertEquals(Integer.valueOf(-1), result.get("byteOffset"));
        assertEquals(Integer.valueOf(100), result.get("lineNr"));
        assertEquals(Integer.valueOf(13), result.get("columnNr"));
    }

    /**
     * Verify that {@link TokenBuffer} can be properly serialized
     * automatically, using the "standard" JSON sample document
     */
    public void testTokenBuffer() throws Exception {
        TokenBuffer __ins_v1 = null;
        // First, copy events from known good source (StringReader)
        JsonParser jp = createParserUsingReader(SAMPLE_DOC_JSON_SPEC);
        __ins_v1 = new TokenBuffer(null, false);
        TokenBuffer tb = __ins_v1;
        while (jp.nextToken() != null) {
            tb.copyCurrentEvent(jp);
        }
        jp.close();
        // Then serialize as String
        String str = serializeAsString(tb);
        tb.close();
        // and verify it looks ok
        verifyJsonSpecSampleDoc(createParserUsingReader(str), true);
        org.helper.Assertions.verify("var._writeContext._child._gotName_195_212", __ins_v1);
    }
}
