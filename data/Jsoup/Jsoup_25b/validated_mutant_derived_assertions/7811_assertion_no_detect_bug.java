// Instrumented at 2025-12-02 03:23:21
package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tag tests.
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class TagTest {

    @Test
    public void isCaseInsensitive() {
        Tag p1 = Tag.valueOf("P");
        Tag p2 = Tag.valueOf("p");
        assertEquals(p1, p2);
    }

    @Test
    public void trims() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf(" p ");
        assertEquals(p1, p2);
    }

    @Test
    public void equality() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertTrue(p1.equals(p2));
        assertTrue(p1 == p2);
    }

    @Test
    public void divSemantics() {
        Tag div = Tag.valueOf("div");
        assertTrue(div.isBlock());
        assertTrue(div.formatAsBlock());
    }

    @Test
    public void pSemantics() {
        Tag p = Tag.valueOf("p");
        assertTrue(p.isBlock());
        assertFalse(p.formatAsBlock());
    }

    @Test
    public void imgSemantics() {
        Tag img = Tag.valueOf("img");
        assertTrue(img.isInline());
        assertTrue(img.isSelfClosing());
        assertFalse(img.isBlock());
    }

    @Test
    public void defaultSemantics() {
        org.jsoup.parser.Tag __ins_v1 = null;
        // not defined
        Tag foo = Tag.valueOf("foo");
        __ins_v1 = Tag.valueOf("FOO");
        Tag foo2 = __ins_v1;
        assertEquals(foo, foo2);
        assertTrue(foo.isInline());
        assertTrue(foo.formatAsBlock());
        org.helper.Assertions.verify("var.inlineTags.elements_63022_254", __ins_v1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfChecksNotNull() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfChecksNotEmpty() {
        Tag.valueOf(" ");
    }
}
