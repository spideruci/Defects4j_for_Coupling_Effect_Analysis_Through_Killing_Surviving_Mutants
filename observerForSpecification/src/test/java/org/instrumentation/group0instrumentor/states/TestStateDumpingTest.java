package org.instrumentation.group0instrumentor.states;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.staticsandpackage.DomainManagement;
import org.staticsandpackage.StaticFieldScanner;
import org.staticsandpackage.StaticInfoRecorder;

import java.io.File;

import static org.instrumentation.annotation.AnnotationInstrumentor.instrumentAnnotation;
import static org.instrumentation.group0instrumentor.Instrumentor.instrumentTestClass;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStateDumpingTest {

    /**
     * This test is used to test the instrumentation of the states, do instrumentation and then run the test org.general.StateDumpingTest
     */
    @Disabled
    @Test
    public void test() {
        try {
            StaticInfoRecorder.clearStaticFields();
            StaticFieldScanner.scanStatics(new File("target/classes"));
            instrumentAnnotation("target/test-classes/org/general/StatesDumpingTest.class");
            instrumentTestClass(new File("target/test-classes/org/general/StatesDumpingTest.class"));
            instrumentAnnotation("target/test-classes/org/general/StatesDumpingTest$InnerPrivateClass.class");
            instrumentTestClass(new File("target/test-classes/org/general/StatesDumpingTest$InnerPrivateClass.class"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStaticFieldScanner1() {
        StaticInfoRecorder.clearStaticFields();
        StaticFieldScanner.scanStatics(new File("target/classes"));
        assertTrue(DomainManagement.domains.contains("org.staticsandpackage.Utils"));

        assertTrue(DomainManagement.packages.contains("org.preruninstrumentation"));
        System.err.println(DomainManagement.packages);
        System.err.println(DomainManagement.domains);
    }
}
