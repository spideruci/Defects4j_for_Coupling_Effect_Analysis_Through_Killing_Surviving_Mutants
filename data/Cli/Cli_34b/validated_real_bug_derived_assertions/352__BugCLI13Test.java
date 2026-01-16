// Instrumented at 2025-12-02 15:33:29
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli.bug;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import junit.framework.TestCase;

/**
 * @author brianegge
 */
public class BugCLI13Test extends TestCase {

    public void testCLI13() throws ParseException {
        PosixParser __ins_v1 = null;
        final String debugOpt = "debug";
        Option debug = OptionBuilder.withArgName(debugOpt).withDescription("turn on debugging").withLongOpt(debugOpt).hasArg().create('d');
        Options options = new Options();
        options.addOption(debug);
        __ins_v1 = new PosixParser();
        CommandLine commandLine = __ins_v1.parse(options, new String[] { "-d", "true" });
        assertEquals("true", commandLine.getOptionValue(debugOpt));
        assertEquals("true", commandLine.getOptionValue('d'));
        assertTrue(commandLine.hasOption('d'));
        assertTrue(commandLine.hasOption(debugOpt));
        org.helper.Assertions.verify("var.currentOption.type_352_", __ins_v1);
    }
}
