// Instrumented at 2025-12-02 09:51:51
/**
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test case for the PatternOptionBuilder class
 *
 * @author Henri Yandell
 */
public class PatternOptionBuilderTest extends TestCase {

    public static void main(String[] args) {
        String[] testName = { PatternOptionBuilderTest.class.getName() };
        junit.textui.TestRunner.main(testName);
    }

    public static TestSuite suite() {
        return new TestSuite(PatternOptionBuilderTest.class);
    }

    public PatternOptionBuilderTest(String s) {
        super(s);
    }

    public void testSimplePattern() {
        PosixParser __ins_v1 = null;
        try {
            Options options = PatternOptionBuilder.parsePattern("a:b@cde>f+n%t/");
            String[] args = new String[] { "-c", "-a", "foo", "-b", "java.util.Vector", "-e", "build.xml", "-f", "java.util.Calendar", "-n", "4.5", "-t", "http://jakarta.apache.org/" };
            /// DATES NOT SUPPORTED YET.
            //      assertEquals("number flag t", new java.util.Date(1023400137276L), line.getOptionObject('z'));
            //     input is:  "Thu Jun 06 17:48:57 EDT 2002"
            __ins_v1 = new PosixParser();
            CommandLineParser parser = __ins_v1;
            CommandLine line = parser.parse(options, args);
            // tests the char methods of CommandLine that delegate to
            // the String methods
            assertEquals("flag a", "foo", line.getOptionValue("a"));
            assertEquals("flag a", "foo", line.getOptionValue('a'));
            assertEquals("string flag a", "foo", line.getOptionObject("a"));
            assertEquals("string flag a", "foo", line.getOptionObject('a'));
            assertEquals("object flag b", new java.util.Vector(), line.getOptionObject("b"));
            assertEquals("object flag b", new java.util.Vector(), line.getOptionObject('b'));
            assertEquals("boolean true flag c", true, line.hasOption("c"));
            assertEquals("boolean true flag c", true, line.hasOption('c'));
            assertEquals("boolean false flag d", false, line.hasOption("d"));
            assertEquals("boolean false flag d", false, line.hasOption('d'));
            assertEquals("file flag e", new java.io.File("build.xml"), line.getOptionObject("e"));
            assertEquals("file flag e", new java.io.File("build.xml"), line.getOptionObject('e'));
            assertEquals("class flag f", java.util.Calendar.class, line.getOptionObject("f"));
            assertEquals("class flag f", java.util.Calendar.class, line.getOptionObject('f'));
            assertEquals("number flag n", new Float(4.5), line.getOptionObject("n"));
            assertEquals("number flag n", new Float(4.5), line.getOptionObject('n'));
            assertEquals("url flag t", new java.net.URL("http://jakarta.apache.org/"), line.getOptionObject("t"));
            assertEquals("url flag t", new java.net.URL("http://jakarta.apache.org/"), line.getOptionObject('t'));
        } catch (ParseException exp) {
            fail(exp.getMessage());
        } catch (java.net.MalformedURLException exp) {
            fail(exp.getMessage());
        }
        org.helper.Assertions.verify("var.cmd_69_", __ins_v1);
    }
}
