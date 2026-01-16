// Instrumented at 2025-12-07 16:09:23
package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class NodeTest extends XmlTestBase {

    public void testMixed() throws Exception {
        java.lang.String __ins_v1 = null;
        final XmlMapper xmlMapper = new XmlMapper();
        final ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode root = xmlMapper.readTree("<root>first<child>4</child>second</root>");
        __ins_v1 = jsonMapper.writeValueAsString(root);
        String json = __ins_v1;
        System.out.println("-> " + json);
        org.helper.Assertions.verify("primitive_1_215", __ins_v1);
    }
}
