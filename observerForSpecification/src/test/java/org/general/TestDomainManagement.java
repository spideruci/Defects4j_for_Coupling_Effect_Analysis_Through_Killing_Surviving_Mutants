package org.general;

import org.junit.jupiter.api.Test;
import org.staticsandpackage.DomainManagement;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDomainManagement {

    public TestDomainManagement() {
        DomainManagement.domains.add("org.commons.my.Hang");
        DomainManagement.domains.add("org.commons.my.utils.Hello");
        DomainManagement.domains.add("org.commons.my.utils.he.World");
        DomainManagement.domains.add("org.commons.my.jj.hello.World");
        DomainManagement.domains.add("org.commons.my.jj.Griffin");

        DomainManagement.packages.add("org.commons.my");
        DomainManagement.packages.add("org.commons.my.utils");
        DomainManagement.packages.add("org.commons.my.utils.he");
        DomainManagement.packages.add("org.commons.my.jj.hello");
        DomainManagement.packages.add("org.commons.my.jj");
    }

    @Test
    public void testGetProjectDomain() {

        //test getProjectDomain
        assertEquals("org.commons.my", DomainManagement.getProjectDomain(DomainManagement.domains));

        //test getPackageDomain
        assertEquals("org.commons.my", DomainManagement.getPackageDomain("org.commons.my.TestHang"));
        assertEquals("org.commons.my", DomainManagement.getPackageDomain("org.commons.my.HangTest"));
        assertEquals("org.commons.my.jj", DomainManagement.getPackageDomain("org.commons.my.jj.nothing.Nothing"));
        assertEquals("org.commons.my", DomainManagement.getPackageDomain("org.somethingelse.haha.haha.HahaClass"));

        //test getClassDomain
        assertEquals("org.commons.my.Hang", DomainManagement.getClassDomain("org.commons.my.TestHang"));
        assertEquals("org.commons.my.jj", DomainManagement.getClassDomain("org.commons.my.jj.nihao.wobuhao.HangTest"));
        assertEquals("org.commons.my", DomainManagement.getClassDomain("org.nothing.nothing.HahaTest"));
    }

//    @Test
//    public void testsomething() {
//        assertTrue("ksaljdf".startsWith(""));
//    }
}
