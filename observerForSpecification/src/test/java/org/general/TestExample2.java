package org.general;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class TestExample2 extends TestExample1{
    @BeforeEach
    public void doBeforeEach() {
        System.err.println("beforeEach2-1");
    }

    @Test
    public void testChainedMethodCalls() {
        ExampleClass e =
            new ExampleClass().
                    getAnotherExampleClass().
                    getAnotherExampleClass();
    }

    @Test
    public void testChainedMethodCalls1() {
        ExampleClass e =
                ExampleClass.getExampleClass().
                        getAnotherExampleClass().
                        getAnotherExampleClass();
    }
}
