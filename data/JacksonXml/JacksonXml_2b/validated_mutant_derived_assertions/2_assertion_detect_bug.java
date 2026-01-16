// Instrumented at 2025-12-07 16:09:24
package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class NodeTest extends XmlTestBase {

    public void testMixed() throws Exception {
        String __ins_v1 = null;
        final XmlMapper xmlMapper = new XmlMapper();
        final ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode root = xmlMapper.readTree("<root>first<child>4</child>second</root>");
        String json = jsonMapper.writeValueAsString(root);
        __ins_v1 = json;
        System.out.println("-> " + json);
        org.helper.Assertions.verify("primitive_2_215", __ins_v1);
    }
}
