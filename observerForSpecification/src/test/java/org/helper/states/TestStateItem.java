package org.helper.states;

import java.util.LinkedList;
import java.util.List;

public class TestStateItem {

    static List<Object> visited = new LinkedList<>();

//
//    @Test
//    public void myTest() {
//        Integer x = 3;
//        BigInteger y = new BigInteger("3");
//
//    }
//    @Test
//    public void testNull() {
//        privateStaticPrintFields(null);
//    }
//
//    @Test
//    public void testInteger() {
//        privateStaticPrintFields(3);
//    }
//
//    @Test
//    public void testGetFields() {
//
//       privateStaticPrintFields(new ObjectFieldExample());
//
//    }
//
//    @Test
//    public void testCollection() {
//        List<ObjectFieldExample> x = new ArrayList<>();
//        x.add(new ObjectFieldExample());
//        x.add(new ObjectFieldExample());
//        printElementsFromIterable(x);
//    }
//
//    @Test
//    public void testPrintMap1() {
//        Map<Object, Object> map = new HashMap<>();
//        map.put(new ObjectFieldExample(), new ObjectFieldExample());
//        printMap(map);
//    }
//
//    @Test
//    public void testPrintMap2() {
//        Map<Object, Object> map = new HashMap<>();
//        map.put(new ObjectFieldExample(), 3);
//        printMap(map);
//    }
//
//    private static void printMap(Object obj) {
//        if (obj instanceof Map) {
//            System.err.println("ha");
//            Map<Object, Object> map = (Map<Object,Object>) obj;
//            //iterate over the map
//            for (Object o : map.keySet()) {
//                System.err.println("key: ");
//                privateStaticPrintFields(o);
//                System.err.println("value: ");
//                privateStaticPrintFields(map.get(o));
//            }
//        }
//    }
//
//    public static void printElementsFromIterable(Object obj) {
//        if (obj instanceof Iterable) {
//            Iterable<Object> iterable = (List<Object>) obj;
//            for (Object o : iterable) {
//                System.err.println("element: ");
//                privateStaticPrintFields(o);
//            }
//        }
//    }
//
//    public static void privateStaticPrintFields(Object obj) {
////        if (obj instanceof Integer || obj instanceof String || obj instanceof Short || obj instanceof Long || obj instanceof Double
////                 || obj instanceof Float || obj instanceof Character || obj instanceof Byte || obj instanceof Boolean || obj == null) {
////            System.err.println(obj);
////            return;
////        } else {
////            visited.add(obj);
////        }
//
//        Class<?> clazz = obj.getClass();
//        System.out.println("Fields of " + clazz.getName() + ":");
//
//        // Get all fields, including private ones
//        Field[] fields = clazz.getDeclaredFields();
//
//        for (Field field : fields) {
//            // Set accessible true to access private fields
//            try {
//                field.setAccessible(true);
//                try {
//
//                    // Get the value of each field
//                    Object value = field.get(obj);
//
//                    System.err.println(field.getName() + " = " + value);
//                    boolean flag = false;
//                    for (Object o : visited) {
//                        if (o == obj) {
//                            flag = true;
//                            break;
//                        }
//                    }
//                    if (flag) {
//
//                    } else {
//                        privateStaticPrintFields(value);
//                    }
//
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            } catch (Exception e) {
//                // ignore, usually inaccessible
//            }
//
//        }
//    }
}
