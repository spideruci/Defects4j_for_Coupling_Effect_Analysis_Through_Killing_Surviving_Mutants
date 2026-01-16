// Instrumented at 2025-12-02 10:34:13
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli2.bug;

import junit.framework.TestCase;
import org.apache.commons.cli2.*;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.validation.FileValidator;

/**
 * @author brianegge
 */
public class BugCLI122Test extends TestCase {

    public void testArgumentWhichStartsWithDash() throws OptionException {
        org.apache.commons.cli2.Group __ins_v1 = null;
        Argument wdArg = new ArgumentBuilder().withName("anything").withMaximum(1).withMinimum(1).withInitialSeparator('=').create();
        Option wdOpt = new DefaultOptionBuilder().withArgument(wdArg).withDescription("anything, foo or -foo").withLongName("argument").withShortName("a").create();
        __ins_v1 = new GroupBuilder().withOption(wdOpt).create();
        Group group = __ins_v1;
        Parser p = new Parser();
        p.setGroup(group);
        CommandLine normal = p.parse(new String[] { "-a", "foo" });
        assertNotNull(normal);
        assertEquals(normal.getValue(wdOpt), "foo");
        CommandLine withDash = p.parse(new String[] { "--argument", "\"-foo\"" });
        assertNotNull(withDash);
        assertEquals("-foo", withDash.getValue(wdOpt));
        CommandLine withDashAndEquals = p.parse(new String[] { "--argument=-foo" });
        assertNotNull(withDashAndEquals);
        assertEquals("-foo", withDashAndEquals.getValue(wdOpt));
        org.helper.Assertions.verify("var.metas_534_", __ins_v1);
    }
}
