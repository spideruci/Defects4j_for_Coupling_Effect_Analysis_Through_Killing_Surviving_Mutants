// Instrumented at 2025-12-13 14:00:42
package com.fasterxml.jackson.databind.node;

import java.io.IOException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class TestEndOfInputHandling extends BaseMapTest {

    public void testErrorHandling() throws IOException {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        String json = "{\"A\":{\"B\":\n";
        JsonParser parser = mapper.getFactory().createParser(json);
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
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_3234_18", __ins_v1);
    }
}
