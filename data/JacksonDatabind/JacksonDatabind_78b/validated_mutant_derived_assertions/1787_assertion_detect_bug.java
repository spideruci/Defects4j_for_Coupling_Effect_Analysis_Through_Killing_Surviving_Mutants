// Instrumented at 2025-12-13 14:00:52
package com.fasterxml.jackson.databind.node;

import java.io.IOException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class TestEndOfInputHandling extends BaseMapTest {

    public void testErrorHandling() throws IOException {
        ObjectMapper __ins_v1 = null;
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"A\":{\"B\":\n";
        JsonParser parser = mapper.getFactory().createParser(json);
        __ins_v1 = new ObjectMapper();
        parser.setCodec(__ins_v1);
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
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_3236_18", __ins_v1);
    }
}
