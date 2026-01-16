// Instrumented at 2025-12-07 12:55:04
package com.fasterxml.jackson.dataformat.xml.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class NodeTest extends XmlTestBase {

    public void testMixed() throws Exception {
        com.fasterxml.jackson.databind.JsonNode __ins_v1 = null;
        final XmlMapper xmlMapper = new XmlMapper();
        final ObjectMapper jsonMapper = new ObjectMapper();
        __ins_v1 = xmlMapper.readTree("<root>first<child>4</child>second</root>");
        JsonNode root = __ins_v1;
        String json = jsonMapper.writeValueAsString(root);
        System.out.println("-> " + json);
        org.helper.Assertions.verify("var.metas_3_", __ins_v1);
    }
}
