package org.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


public class StatesDumpingTest {

    private String name = "haha";

    // we need to instrument the inner class to public so as to access it.
    private static class InnerPrivateClass {
        private int zero = 0;
        public int getZero() {
            return zero;
        }

        private String getString() {
            return "4345";
        }
    }

    private Group0Example e;

    @BeforeEach
    public void doBeforeEach() {
        e = new Group0Example();
    }

    @Test
    public void testInt() {
        new InnerPrivateClass().getZero();
        e.getInteger();
    }

    @Test
    public void testBoolean() {
        e.getBoolean();
    }

    @Test
    public void testChar() {
        e.getChar();
    }

    @Test
    public void testByte() {
        e.getByte();
    }

    @Test
    public void testShort() {
        e.getShort();
    }

    @Test
    public void testLong() {
        e.getLong();
    }

    @Test
    public void testDouble() {
        e.getDouble();
    }

    @Test
    public void testFloat() {
        e.getFloat();
    }

    @Test
    public void testNullObject() {
        e.getNullObject();
    }

    @Test
    public void testNonNullObject() {
        e.getNonNullObject();
    }


    @Test
    public void testNonObjectObject() {
        new ExampleClass().getZero();
    }
    //BUG: Process Object is not correct
//    private static void doSomething () {
//        processBoolean(true, "line_loc", "dklasjf");
//    }


    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    public void testParam(int x) {
        System.err.println(x);
    }
}
