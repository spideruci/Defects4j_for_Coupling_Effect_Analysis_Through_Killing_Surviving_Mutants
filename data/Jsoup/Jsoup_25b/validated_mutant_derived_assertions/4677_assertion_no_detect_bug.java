// Instrumented at 2025-12-02 03:18:28
package org.jsoup.helper;

import static org.junit.Assert.assertEquals;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DataUtilTest {

    @Test
    public void testCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html;charset=utf-8 "));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals(null, DataUtil.getCharsetFromContentType("text/html"));
        assertEquals(null, DataUtil.getCharsetFromContentType(null));
    }

    @Test
    public void testQuotedCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html;charset=\"utf-8\""));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
    }

    @Test
    public void discardsSpuriousByteOrderMark() {
        org.jsoup.nodes.Document __ins_v1 = null;
        String html = "\uFEFF<html><head><title>One</title></head><body>Two</body></html>";
        ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
        __ins_v1 = DataUtil.parseByteData(buffer, "UTF-8", "http://foo.com/", Parser.htmlParser());
        Document doc = __ins_v1;
        assertEquals("One", doc.head().text());
        org.helper.Assertions.verify("var.tag.inlineTags.elements_36331_256", __ins_v1);
    }
}
