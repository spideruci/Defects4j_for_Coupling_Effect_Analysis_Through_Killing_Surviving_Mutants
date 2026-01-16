// Instrumented at 2025-12-10 19:59:20
package com.fasterxml.jackson.databind.jsontype.ext;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

public class ExternalTypeId999Test extends BaseMapTest {

    public static interface Payload {
    }

    @JsonTypeName("foo")
    public static class FooPayload implements Payload {
    }

    @JsonTypeName("bar")
    public static class BarPayload implements Payload {
    }

    public static class Message<P extends Payload> {

        final String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({ @JsonSubTypes.Type(FooPayload.class), @JsonSubTypes.Type(BarPayload.class) })
        private final P payload;

        @JsonCreator
        public Message(@JsonProperty("type") String type, @JsonProperty("payload") P payload) {
            this.type = type;
            this.payload = payload;
        }
    }

    private final ObjectMapper MAPPER = objectMapper();

    public void testExternalTypeId() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        TypeReference<?> type = new TypeReference<Message<FooPayload>>() {
        };
        __ins_v1 = MAPPER;
        Message<?> msg = __ins_v1.readValue(aposToQuotes("{ 'type':'foo', 'payload': {} }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
        // and then with different order
        msg = MAPPER.readValue(aposToQuotes("{'payload': {}, 'type':'foo' }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
        org.helper.Assertions.verify("var._deserializationContext._factory_1097_", __ins_v1);
    }
}
