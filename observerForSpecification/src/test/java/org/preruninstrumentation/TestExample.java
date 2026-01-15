package org.preruninstrumentation;

import org.general.BaseExampleClass;
import org.general.ExampleClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExample {

    @Test
    public void test1() {
        BaseExampleClass b = new ExampleClass();
        assertEquals(11, b.getOne());
        assertEquals(0, b.getZero());
    }

    @Test
    public void test2() {
        int x = 0;
        String y = String.valueOf(x);
    }
}
