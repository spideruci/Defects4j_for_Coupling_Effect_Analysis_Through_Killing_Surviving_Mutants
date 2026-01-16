// Instrumented at 2025-12-02 15:33:32
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
package org.apache.commons.cli;

import junit.framework.TestCase;

public class ArgumentIsOptionTest extends TestCase {

    private Options options = null;

    private CommandLineParser parser = null;

    public void setUp() {
        options = new Options().addOption("p", false, "Option p").addOption("attr", true, "Option accepts argument");
        parser = new PosixParser();
    }

    public void testOptionAndOptionWithArgument() throws Exception {
        org.apache.commons.cli.CommandLineParser __ins_v1 = null;
        String[] args = new String[] { "-p", "-attr", "p" };
        __ins_v1 = parser;
        CommandLine cl = __ins_v1.parse(options, args);
        assertTrue("Confirm -p is set", cl.hasOption("p"));
        assertTrue("Confirm -attr is set", cl.hasOption("attr"));
        assertTrue("Confirm arg of -attr", cl.getOptionValue("attr").equals("p"));
        assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
        org.helper.Assertions.verify("var.currentOption.argName_257_", __ins_v1);
    }

    public void testOptionWithArgument() throws Exception {
        String[] args = new String[] { "-attr", "p" };
        CommandLine cl = parser.parse(options, args);
        assertFalse("Confirm -p is set", cl.hasOption("p"));
        assertTrue("Confirm -attr is set", cl.hasOption("attr"));
        assertTrue("Confirm arg of -attr", cl.getOptionValue("attr").equals("p"));
        assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
    }

    public void testOption() throws Exception {
        String[] args = new String[] { "-p" };
        CommandLine cl = parser.parse(options, args);
        assertTrue("Confirm -p is set", cl.hasOption("p"));
        assertFalse("Confirm -attr is not set", cl.hasOption("attr"));
        assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
    }
}
