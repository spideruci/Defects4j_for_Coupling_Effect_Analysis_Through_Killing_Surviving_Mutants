// Instrumented at 2025-12-08 15:57:01
package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test TextNodes
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class TextNodeTest {

    @Test
    public void testBlank() {
        TextNode one = new TextNode("", "");
        TextNode two = new TextNode("     ", "");
        TextNode three = new TextNode("  \n\n   ", "");
        TextNode four = new TextNode("Hello", "");
        TextNode five = new TextNode("  \nHello ", "");
        assertTrue(one.isBlank());
        assertTrue(two.isBlank());
        assertTrue(three.isBlank());
        assertFalse(four.isBlank());
        assertFalse(five.isBlank());
    }

    @Test
    public void testTextBean() {
        org.jsoup.nodes.Node __ins_v1 = null;
        Document doc = Jsoup.parse("<p>One <span>two</span> three</p>");
        Element p = doc.select("p").first();
        __ins_v1 = p.childNode(2);
        TextNode tn = (TextNode) __ins_v1;
        assertEquals(" three", tn.text());
        tn.text(" POW!");
        assertEquals("One <span>two</span> POW!", p.html());
        org.helper.Assertions.verify("var.parentNode.tag.tags_357_569", __ins_v1);
    }
}
