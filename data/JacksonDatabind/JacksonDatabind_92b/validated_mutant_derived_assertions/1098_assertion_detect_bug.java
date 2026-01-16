// Instrumented at 2025-12-01 00:17:10
package com.fasterxml.jackson.databind.objectid;

import java.util.concurrent.atomic.AtomicReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;

public class ReferentialWithObjectIdTest extends BaseMapTest {

    public static class EmployeeList {

        public AtomicReference<Employee> first;
    }

    @JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator.class)
    public static class Employee {

        public int id;

        public String name;

        public AtomicReference<Employee> next;

        public Employee next(Employee n) {
            next = new AtomicReference<Employee>(n);
            return this;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testAtomicWithObjectId() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        Employee first = new Employee();
        first.id = 1;
        first.name = "Alice";
        Employee second = new Employee();
        second.id = 2;
        second.name = "Bob";
        first.next(second);
        second.next(first);
        EmployeeList input = new EmployeeList();
        input.first = new AtomicReference<Employee>(first);
        __ins_v1 = MAPPER;
        String json = __ins_v1.writeValueAsString(input);
        // and back
        EmployeeList result = MAPPER.readValue(json, EmployeeList.class);
        Employee firstB = result.first.get();
        assertNotNull(firstB);
        assertEquals("Alice", firstB.name);
        Employee secondB = firstB.next.get();
        assertNotNull(secondB);
        assertEquals("Bob", secondB.name);
        assertNotNull(secondB.next.get());
        assertSame(firstB, secondB.next.get());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1759_32", __ins_v1);
    }
}
