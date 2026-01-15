package org.instrumentation.group0instrumentor.states;

import org.helper.TestExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTestExtension {

    @ParameterizedTest
    @CsvSource({
            "12, 4",
            "11, 4",
            "10, 4",
            "9, 4",
            "8, 4",
            "7, 4",
            "4, 4",
    })
    void testSplitNum(int x, int y) {
        Set<Integer> result = TestExtension.splitItems(x, y);
        assertTrue(result.contains(x));
        assertEquals(y, result.size());
    }


    @ParameterizedTest
    @CsvSource({
            "1, 4",
            "2, 4",
            "3, 4",
    })
    void testSplitNumSmallX(int x, int y) {
        Set<Integer> result = TestExtension.splitItems(x, y);
        assertTrue(result.contains(x));
        assertEquals(x, result.size());
    }
}
