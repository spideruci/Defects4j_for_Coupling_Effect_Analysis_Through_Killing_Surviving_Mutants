// Instrumented at 2025-12-10 19:59:17
package com.fasterxml.jackson.databind.struct;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.*;

public class TestUnwrappedWithSameName647 extends BaseMapTest {

    static class UnwrappedWithSamePropertyName {

        public MailHolder mail;
    }

    static class MailHolder {

        @JsonUnwrapped
        public Mail mail;
    }

    static class Mail {

        public String mail;
    }

    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testUnwrappedWithSamePropertyName() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        final String JSON = "{'mail': {'mail': 'the mail text'}}";
        __ins_v1 = MAPPER;
        UnwrappedWithSamePropertyName result = __ins_v1.readValue(aposToQuotes(JSON), UnwrappedWithSamePropertyName.class);
        assertNotNull(result.mail);
        assertNotNull(result.mail.mail);
        assertEquals("the mail text", result.mail.mail.mail);
        org.helper.Assertions.verify("var._deserializationContext._factory_1615_", __ins_v1);
    }
}
