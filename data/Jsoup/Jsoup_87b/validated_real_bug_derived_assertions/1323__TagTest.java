// Instrumented at 2025-12-06 11:16:33
package org.jsoup.parser;

import org.jsoup.MultiLocaleRule;
import org.jsoup.MultiLocaleRule.MultiLocaleTest;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tag tests.
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class TagTest {

    @Rule
    public MultiLocaleRule rule = new MultiLocaleRule();

    @Test
    public void isCaseSensitive() {
        Tag p1 = Tag.valueOf("P");
        Tag p2 = Tag.valueOf("p");
        assertFalse(p1.equals(p2));
    }

    @Test
    @MultiLocaleTest
    public void canBeInsensitive() {
        Tag script1 = Tag.valueOf("script", ParseSettings.htmlDefault);
        Tag script2 = Tag.valueOf("SCRIPT", ParseSettings.htmlDefault);
        assertSame(script1, script2);
    }

    @Test
    public void trims() {
        org.jsoup.parser.Tag __ins_v1 = null;
        __ins_v1 = Tag.valueOf("p");
        Tag p1 = __ins_v1;
        Tag p2 = Tag.valueOf(" p ");
        assertEquals(p1, p2);
        org.helper.Assertions.verify("var.metas_1323_", __ins_v1);
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
        // not defined
        Tag foo = Tag.valueOf("FOO");
        Tag foo2 = Tag.valueOf("FOO");
        assertEquals(foo, foo2);
        assertTrue(foo.isInline());
        assertTrue(foo.formatAsBlock());
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
