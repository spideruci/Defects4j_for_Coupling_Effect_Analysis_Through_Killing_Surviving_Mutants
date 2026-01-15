package org.general;

import org.helper.TestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

//@ExtendWith(TestExtension.class)
public class TestExample1 {

    public static int x = 10;

    private int y = 3;

    @BeforeEach
    public void doBeforeEach() {
        System.err.println("beforeEach");
    }

    @Test
    public void test1() {
        x = 2;
        System.err.println("test1");
    }

    @Test
    public void test2() {
        x = 3;
        System.err.println("test2");
    }

    public String toString() {
        return "TestExample1: " + x;
    }


}
