// Instrumented at 2026-01-02 00:34:09
package com.fasterxml.jackson.databind.node;

import java.io.IOException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class TestEndOfInputHandling extends BaseMapTest {

    public void testErrorHandling() throws IOException {
        com.fasterxml.jackson.core.JsonParser __ins_v1 = null;
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"A\":{\"B\":\n";
        __ins_v1 = mapper.getFactory().createParser(json);
        JsonParser parser = __ins_v1;
        parser.setCodec(new ObjectMapper());
        try {
            parser.readValueAsTree();
        } catch (JsonParseException e) {
            verifyException(e, "Unexpected end-of-input");
        }
        parser.close();
        try {
            mapper.readTree(json);
        } catch (JsonParseException e) {
            verifyException(e, "Unexpected end-of-input");
        }
        org.helper.Assertions.verify("var._symbols._symbols.elements_245_1", __ins_v1);
    }
}
