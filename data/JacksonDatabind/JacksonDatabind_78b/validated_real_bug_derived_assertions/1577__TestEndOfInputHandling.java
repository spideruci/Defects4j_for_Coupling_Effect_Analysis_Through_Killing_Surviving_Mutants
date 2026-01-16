// Instrumented at 2025-12-10 19:59:21
package com.fasterxml.jackson.databind.node;

import java.io.IOException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class TestEndOfInputHandling extends BaseMapTest {

    public void testErrorHandling() throws IOException {
        com.fasterxml.jackson.core.JsonFactory __ins_v1 = null;
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"A\":{\"B\":\n";
        __ins_v1 = mapper.getFactory();
        JsonParser parser = __ins_v1.createParser(json);
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
        org.helper.Assertions.verify("var._objectCodec._deserializationContext._factory_1577_", __ins_v1);
    }
}
