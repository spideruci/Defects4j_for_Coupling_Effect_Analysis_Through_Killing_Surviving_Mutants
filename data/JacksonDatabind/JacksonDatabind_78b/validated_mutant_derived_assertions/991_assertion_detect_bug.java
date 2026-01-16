// Instrumented at 2025-12-13 14:00:46
package com.fasterxml.jackson.databind.misc;

import java.io.IOException;
import java.security.Permission;
import com.fasterxml.jackson.databind.*;

// Test(s) to verify that forced access works as expected
public class AccessFixTest extends BaseMapTest {

    static class CauseBlockingSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) throws SecurityException {
            if ("suppressAccessChecks".equals(perm.getName())) {
                throw new SecurityException("Can not force permission: " + perm);
            }
        }
    }

    // [databind#877]: avoid forcing access to `cause` field of `Throwable`
    // as it is never actually used (always call `initCause()` instead)
    public void testCauseOfThrowableIgnoral() throws Exception {
        ObjectMapper __ins_v1 = null;
        final SecurityManager origSecMan = System.getSecurityManager();
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            _testCauseOfThrowableIgnoral(mapper);
        } finally {
            System.setSecurityManager(origSecMan);
        }
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_2046_18", __ins_v1);
    }

    private void _testCauseOfThrowableIgnoral(ObjectMapper mapper) throws Exception {
        IOException e = mapper.readValue("{}", IOException.class);
        assertNotNull(e);
    }
}
