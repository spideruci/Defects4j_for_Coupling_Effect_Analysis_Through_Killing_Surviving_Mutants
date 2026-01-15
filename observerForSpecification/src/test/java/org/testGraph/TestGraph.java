package org.testGraph;

import org.general.BaseExampleClass;
import org.general.ExampleClass;
import org.helper.graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TestGraph {


    @Override
    public String toString() {
        return "TestGraph";
    }

    @Test
    public void testNull() {
        Graph g = new Graph(null,10);
        g.prettyPrint();
    }

    @Test
    public void testObject() {
        Graph g = new Graph(new Object(),10);
        g.prettyPrint();
    }

    @Test
    public void testString() {
        Graph g = new Graph("kdslja",10);
        g.prettyPrint();
    }

    @Test
    public void testPrimitive() {
        Graph g = new Graph(1,10);
        g.prettyPrint();
    }

    @Test
    public void testExample() {
        BaseExampleClass b = new ExampleClass();
        Graph g = new Graph(b,10);
        g.prettyPrint();
    }

    @Test
    public void testExampleList() {

        List<BaseExampleClass> list= new LinkedList<>();
        list.add(new ExampleClass());
        list.add(new BaseExampleClass());
        Graph g = new Graph(list,10);
        g.prettyPrint();
    }

    @Test
    public void testExampleListWithNull() {
        List<BaseExampleClass> list= new LinkedList<>();
        list.add(new ExampleClass());
        list.add(null);
        Graph g = new Graph(list,10);
        g.prettyPrint();
    }

    @Test
    public void testExampleListSameElement() {

        List<BaseExampleClass> list= new LinkedList<>();
        BaseExampleClass b = new ExampleClass();
        list.add(b);
        list.add(b);
        Graph g = new Graph(list,10);
        g.prettyPrint();
    }

    @Test
    public void testExampleEmptyMap() {
        Map<BaseExampleClass, Integer> map= new HashMap<>();
        Graph g = new Graph(map,10);
        g.prettyPrint();
    }

    @Test
    public void testExampleMap1() {
        Map<BaseExampleClass, Integer> map= new HashMap<>();
        map.put(new ExampleClass(), 1);
        map.put(new ExampleClass(), 5);
        Graph g = new Graph(map, 10);
        g.prettyPrint();
    }

    @Test
    public void testExampleMap2() {
        Map<Integer, ExampleClass> map= new HashMap<>();
        map.put(1, new ExampleClass());
        map.put(4, null);
        Graph g = new Graph(map,10);
        g.prettyPrint();
    }

    @Test
    public void testExampleMap3() {
        Map<ExampleClass, BaseExampleClass> map= new HashMap<>();
        map.put(new ExampleClass(), new ExampleClass());
        ExampleClass b = new ExampleClass();
        map.put(b,b);
        Graph g = new Graph(map,10);
        g.prettyPrint();
    }

    @Test
    public void testNestedStructure() {
        Map<BaseExampleClass, List<BaseExampleClass>> map= new HashMap<>();
        List<BaseExampleClass> list = new LinkedList<>();
        list.add(new ExampleClass());
        list.add(new BaseExampleClass());
        map.put(new ExampleClass(), list);
        Graph g = new Graph(map,10);
        g.prettyPrint();
    }

    @Test
    public void testNonPrimitiveArray() {
        String[][] a = new String[2][2];
        a[0][0] = "1";
        a[0][1] = "2";
        a[1][0] = "3";
        a[1][1] = "4";
        Graph g = new Graph(a,10);
        g.prettyPrint();
    }

    @Test
    public void testPrimitiveArray() {
        int[] a = new int[1];
        a[0] = 2;
        Graph g = new Graph(a,10);
        System.err.println();
        g.prettyPrint();
    }


    @Test
    public void testBoxedInteger() {
        Integer x = 3;
        Graph g = new Graph(x,10);
        g.prettyPrint();
    }

    @Test
    public void testExample3() {
        String[] x1 = new String[2];
        x1[0] = "1";
        x1[1] = "2";
        String[] x2 = new String[2];
        x2[0] = "1";
        x2[1] = "2";

        String s1 = new String("hello");
        String s2 = new String("hello");

        System.err.println(s1 == s2);
        System.err.println(s1.equals(s2));
    }

    @Test
    public void testSortedSet() {
        Set<String> s = new TreeSet<>();
        s.add("3");
        s.add("2");
//        s.add(new ExampleClass());
//        s.add(new ExampleClass());
//        s.add(new BaseExampleClass());

//        System.err.println(s.size());
        Graph g = new Graph(s,3);
        g.prettyPrint();

    }

    @Test
    public void testNonComparableSet1() {
        Set<BaseExampleClass> s = new HashSet<>();
        s.add(new ExampleClass());
        s.add(new ExampleClass());
        s.add(new BaseExampleClass());
        Graph g = new Graph(s,3);
        g.prettyPrint();
    }

    @Test
    public void testNonComparableSet2() {
        Set<BaseExampleClass> s = new HashSet<>();
        s.add(new ExampleClass());
        s.add(new ExampleClass());
        s.add(new BaseExampleClass());
        Graph g = new Graph(s,3);
        g.prettyPrint();
    }

    @Test
    public void testSet1() {
        Set<Integer> s = new HashSet<>();
        s.add(3);
        s.add(2);
        s.add(1);
        Graph g = new Graph(s,3);
        g.prettyPrint();
    }

}
