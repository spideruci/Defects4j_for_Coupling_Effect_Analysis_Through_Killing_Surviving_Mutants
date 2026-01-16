// Instrumented at 2025-12-08 15:57:04
package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Document.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class DocumentTest {

    @Test
    public void setTextPreservesDocumentStructure() {
        org.jsoup.select.Elements __ins_v1 = null;
        Document doc = Jsoup.parse("<p>Hello</p>");
        doc.text("Replaced");
        assertEquals("Replaced", doc.text());
        assertEquals("Replaced", doc.body().text());
        __ins_v1 = doc.select("head");
        assertEquals(1, __ins_v1.size());
        org.helper.Assertions.verify("var.elements.tag.tags_172_569", __ins_v1);
    }

    @Test
    public void testTitles() {
        Document noTitle = Jsoup.parse("<p>Hello</p>");
        Document withTitle = Jsoup.parse("<title>First</title><title>Ignore</title><p>Hello</p>");
        assertEquals("", noTitle.title());
        noTitle.title("Hello");
        assertEquals("Hello", noTitle.title());
        assertEquals("Hello", noTitle.select("title").first().text());
        assertEquals("First", withTitle.title());
        withTitle.title("Hello");
        assertEquals("Hello", withTitle.title());
        assertEquals("Hello", withTitle.select("title").first().text());
    }
}
