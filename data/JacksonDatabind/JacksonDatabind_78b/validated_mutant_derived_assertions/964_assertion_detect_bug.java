// Instrumented at 2025-12-13 14:00:45
package com.fasterxml.jackson.databind.module;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;

public class TestDuplicateRegistration extends BaseMapTest {

    static class MyModule extends com.fasterxml.jackson.databind.Module {

        public int regCount;

        public MyModule() {
            super();
        }

        @Override
        public String getModuleName() {
            return "TestModule";
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext context) {
            ++regCount;
        }
    }

    public void testDuplicateRegistration() throws Exception {
        ObjectMapper __ins_v1 = null;
        // by default, duplicate registration should be prevented
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.isEnabled(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS));
        MyModule module = new MyModule();
        mapper.registerModule(module);
        mapper.registerModule(module);
        mapper.registerModule(module);
        assertEquals(1, module.regCount);
        // but may be allowed by changing setting
        mapper.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        mapper.registerModule(module);
        assertEquals(2, module.regCount);
        __ins_v1 = new ObjectMapper();
        // and ditto for a new instance
        ObjectMapper mapper2 = __ins_v1;
        mapper2.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        MyModule module2 = new MyModule();
        mapper.registerModule(module2);
        mapper.registerModule(module2);
        mapper.registerModule(module2);
        assertEquals(3, module2.regCount);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1943_18", __ins_v1);
    }
}
