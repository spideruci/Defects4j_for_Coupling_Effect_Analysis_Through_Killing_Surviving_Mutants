// Instrumented at 2025-12-08 15:57:01
package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests Nodes
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class NodeTest {

    @Test
    public void handlesBaseUri() {
        Tag tag = Tag.valueOf("a");
        Attributes attribs = new Attributes();
        attribs.put("relHref", "/foo");
        attribs.put("absHref", "http://bar/qux");
        Element noBase = new Element(tag, "", attribs);
        // with no base, should NOT fallback to href attrib, whatever it is
        assertEquals("", noBase.absUrl("relHref"));
        // no base but valid attrib, return attrib
        assertEquals("http://bar/qux", noBase.absUrl("absHref"));
        Element withBase = new Element(tag, "http://foo/", attribs);
        // construct abs from base + rel
        assertEquals("http://foo/foo", withBase.absUrl("relHref"));
        // href is abs, so returns that
        assertEquals("http://bar/qux", withBase.absUrl("absHref"));
        assertEquals("", withBase.absUrl("noval"));
        Element dodgyBase = new Element(tag, "wtf://no-such-protocol/", attribs);
        // base fails, but href good, so get that
        assertEquals("http://bar/qux", dodgyBase.absUrl("absHref"));
        // base fails, only rel href, so return nothing
        assertEquals("", dodgyBase.absUrl("relHref"));
    }

    @Test
    public void handlesAbsPrefix() {
        Document doc = Jsoup.parse("<a href=/foo>Hello</a>", "http://jsoup.org/");
        Element a = doc.select("a").first();
        assertEquals("/foo", a.attr("href"));
        assertEquals("http://jsoup.org/foo", a.attr("abs:href"));
        // only realised on the get method, not in has or iterator
        assertFalse(a.hasAttr("abs:href"));
    }

    @Test
    public void testRemove() {
        Document doc = Jsoup.parse("<p>One <span>two</span> three</p>");
        Element p = doc.select("p").first();
        p.childNode(0).remove();
        assertEquals("two three", p.text());
        assertEquals("<span>two</span> three", p.html());
    }

    @Test
    public void testReplace() {
        org.jsoup.nodes.Element __ins_v1 = null;
        Document doc = Jsoup.parse("<p>One <span>two</span> three</p>");
        __ins_v1 = doc.select("p").first();
        Element p = __ins_v1;
        Element insert = doc.createElement("em").text("foo");
        p.childNode(1).replaceWith(insert);
        assertEquals("One <em>foo</em> three", p.html());
        org.helper.Assertions.verify("var.tag.tags_438_569", __ins_v1);
    }
}
