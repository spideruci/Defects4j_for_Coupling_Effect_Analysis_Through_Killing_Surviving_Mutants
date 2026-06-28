package org.helper.graph;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static org.helper.Utils.addErrorMessage;
import static org.helper.Utils.getStackTraceInfo;

public class Graph {

    V root = null;
    
    private int counter = 0;

    LinkedList<Object> visited = new LinkedList<Object>();

    LinkedList<Pair> nextNode = new LinkedList<Pair>();

    private static Pattern addressPattern = Pattern.compile(".*[\\w+\\.]+@[abcdef\\d]+.*", Pattern.MULTILINE);

    public Graph(Object o, int depth) {
        try {
//            addErrorMessage("Start Graph constructor1");
            BFS(o, depth);
        } catch (Exception e) {
            root = new Leaf("error", "null", 0, "null", false);
            addErrorMessage("fail to generate nodes for a variable\n" + getStackTraceInfo(e));
        }
    }

    void BFS(Object o, int depth) {

        if (isSimpleObject(o)) {
            if (o == null) {
                root = new Leaf("root", "null", 0, "" + o, true);
            } else {
                root = new Leaf("root", o.getClass().getName(), 0, "" + o, true);
            }
            return;
        }

        if (o.getClass().getName().startsWith("java.") || o.getClass().getName().startsWith("javax.")
                || o.getClass().getName().startsWith("sun.")) {
            root = new Leaf("root", o.getClass().getName(), 0, "" + o.toString().replaceAll("@[0-9a-fA-F]+$", ""), true);
            return;
        }

        if (o instanceof Map ) {
            root = new VMap("root", o.getClass().getName(), 0, true);
        } else if (o instanceof Iterable) {
            root = new VCollection("root", o.getClass().getName(), 0, true);
        } else if (o.getClass().isArray()) {
            root = new VCollection("root", o.getClass().getName(), 0, true);
        } else {
//            addErrorMessage("root");
            root = new VNormal("root", o.getClass().getName(), 0, true);
        }
        nextNode.add(new Pair(o, root));

        while (nextNode.size() != 0) {
            Pair p  = nextNode.poll();
            V node = p.getNode();
            Object obj = p.getObj();
//            addErrorMessage("node depth" + node.getDepth());
//            addErrorMessage("here node size " + nextNode.size());
            processNode(obj, node, depth);
        }

    }

    /**
     * It is tolerable that sometimes an object graph is not complete, i.e., some objects are not appropriately serialized, just skip that portion
     * Map and Set is sorted (if possible)
     * @param obj
     * @param node
     * @param depth
     */
    public void processNode(Object obj, V node, int depth) {
        counter+= 1;
//        addErrorMessage("processing node " + counter + " of type " + node.getType() + " at depth " + node.getDepth() + "visited: " + visited.size());
        if (node.getDepth() > depth) {
            // put too much pressure on the serializer system; depth of 10 is the limit
            return;
        }

        if (node instanceof VMap) {
            Iterator iterator;
            VMap myNode = (VMap) node;

            try {
                Set s = ((Map)obj).keySet();
                List<Integer> sortedList = new ArrayList<Integer>(s);
                Collections.sort(sortedList);
                iterator = sortedList.iterator();
            } catch (Exception e) {
                iterator = ((Map)obj).keySet().iterator();
            }
            try {
                while (iterator.hasNext()) {
                    Object key = iterator.next();
                    Object value = ((Map) obj).get(key);
                    V keyNode;
                    V valueNode;
                    if (isSimpleObject(key)) {
                        String type;
                        if (key != null) {
                            type = key.getClass().getName();
                        } else {
                            type = "null";
                        }
                        keyNode = new Leaf("key", type, myNode.getDepth() + 1, "" + key,false);
                    } else if (containsReference(key, visited)) {
                        keyNode = new Leaf("key", key.getClass().getName(), myNode.getDepth() + 1, "visited", false);
                    } else if (key instanceof Iterable || key.getClass().isArray()) {
                        keyNode = new VCollection("key", key.getClass().getName(), myNode.getDepth() + 1, false);
                        nextNode.add(new Pair(key ,keyNode));
                        visited.add(key);
                    } else if (key instanceof Map) {
                        keyNode = new VMap("key", key.getClass().getName(), myNode.getDepth() + 1, false);
                        nextNode.add(new Pair(key, keyNode));
                        visited.add(key);
                    } else {
                        keyNode = new VNormal("key", key.getClass().getName(), myNode.getDepth() + 1, false);
                        nextNode.add(new Pair(key,keyNode));
                        visited.add(key);
                    }

                    if (isSimpleObject(value)) {
                        String type;
                        if (value != null ) {
                            type = value.getClass().getName();
                        } else {
                            type = "null";
                        }
                        valueNode = new Leaf("value", type, myNode.getDepth() + 1, "" + value,false);

                    } else if (containsReference(value, visited)) {
                        valueNode = new Leaf("value", value.getClass().getName(), myNode.getDepth() + 1, "visited",false);
                    } else if (value instanceof Iterable || value.getClass().isArray()) {
                        valueNode = new VCollection("value", value.getClass().getName(), myNode.getDepth() + 1,false);
                        nextNode.add(new Pair(value, valueNode));
                        visited.add(value);
                    } else if (value instanceof Map) {
                        valueNode = new VMap("value", value.getClass().getName(), myNode.getDepth() + 1,false);
                        nextNode.add(new Pair(value, valueNode));
                        visited.add(value);
                    } else {
                        valueNode = new VNormal("value", value.getClass().getName(), myNode.getDepth() + 1, false);
                        nextNode.add(new Pair(value, valueNode));
                        visited.add(value);
                    }
                    myNode.addKVPair(keyNode, valueNode);
                }
            } catch (Exception e) {
                addErrorMessage("when processing map in processNode\n" + getStackTraceInfo(e));
            }


        } else if (node instanceof VCollection) {

            try {
                VCollection myNode = (VCollection) node;
                Iterable iterable;
                if (obj.getClass().isArray()) {
                    String name = obj.getClass().getName();
                    if (name.length() == 2) {
                        // is primitive array
                        myNode.addNode(new Leaf("primitive_array", "primitive_array", myNode.getDepth() + 1, getPrimitiveArrayString(obj),false));
                        visited.add(myNode);
                        return;
                    } else {

                        List<Object> list = Arrays.asList((Object[]) obj);
                        Iterator<Object> iterator = list.iterator();
                        while (iterator.hasNext()) {
                            Object iter = iterator.next();
                            if (isSimpleObject(iter)) {
                                String type;
                                if (iter != null) {
                                    type = iter.getClass().getName();
                                } else {
                                    type = "null";
                                }
                                myNode.addNode(new Leaf("element", type, myNode.getDepth() + 1, "" + iter,false));

                            } else if (containsReference(iter, visited)) {
                                myNode.addNode(new Leaf("element", iter.getClass().getName(), myNode.getDepth() + 1, "visited",false));
                            } else if (iter instanceof Iterable || iter.getClass().isArray()) {
                                V newNode = new VCollection("element", iter.getClass().getName(), myNode.getDepth() + 1,false);
                                myNode.addNode(newNode);
                                nextNode.add(new Pair(iter, newNode));
                                visited.add(iter);
                            } else if (iter instanceof Map) {
                                V newNode = new VMap("element", iter.getClass().getName(), myNode.getDepth() + 1,false);
                                myNode.addNode(newNode);
                                nextNode.add(new Pair(iter, newNode));
                                visited.add(iter);
                            } else {
                                V newNode = new VNormal("element", iter.getClass().getName(), myNode.getDepth() + 1,false);
                                myNode.addNode(newNode);
                                nextNode.add(new Pair(iter, newNode));
                                visited.add(iter);
                            }
                        }



//                    for (Object iter: (Object[]) obj) {
//
//                    }
                        // is non-primitive array
                        return;
                    }
                } else if (obj instanceof Set) {
                    try {
                        // compare to method implemented
                        List<Integer> sortedList = new ArrayList<Integer>((Set) obj);
                        Collections.sort(sortedList);
                        iterable = sortedList;
                    } catch (Exception e) {
                        // compareTo method not-implemented
                        iterable = (Iterable) obj;
                    }
                } else {

                    iterable = (Iterable) obj;
                }


                Iterator<Object> it = iterable.iterator();
                while (it.hasNext()) {
                    Object iter = it.next();
                    if (isSimpleObject(iter)) {
                        String type;
                        if (iter != null) {
                            type = iter.getClass().getName();
                        } else {
                            type = "null";
                        }
                        myNode.addNode(new Leaf("element", type, myNode.getDepth() + 1, "" + iter,false));

                    } else if (containsReference(iter, visited)) {
                        myNode.addNode(new Leaf("element", iter.getClass().getName(), myNode.getDepth() + 1, "visited",false));
                    } else if (iter instanceof Iterable || iter.getClass().isArray()) {
                        V newNode = new VCollection("element", iter.getClass().getName(), myNode.getDepth() + 1,false);
                        myNode.addNode(newNode);
                        nextNode.add(new Pair(iter, newNode));
                        visited.add(iter);
                    } else if (iter instanceof Map) {
                        V newNode = new VMap("element", iter.getClass().getName(), myNode.getDepth() + 1,false);
                        myNode.addNode(newNode);
                        nextNode.add(new Pair(iter, newNode));
                        visited.add(iter);
                    } else {
                        V newNode = new VNormal("element", iter.getClass().getName(), myNode.getDepth() + 1,false);
                        myNode.addNode(newNode);
                        nextNode.add(new Pair(iter, newNode));
                        visited.add(iter);
                    }
                }
            } catch (Exception e) {
                addErrorMessage("when processing collection in processNode\n" + getStackTraceInfo(e));
            }


        } else {
            if (obj.getClass().getName().startsWith("java.") || obj.getClass().getName().startsWith("javax.")
                    || obj.getClass().getName().startsWith("sun.")) {
                VNormal myNode = (VNormal) node;
                myNode.addField("name", new Leaf("name", "toString", myNode.getDepth(), String.valueOf(obj).replaceAll("@[0-9a-fA-F]+$", ""), true));
            } else {

                try {
                    VNormal myNode = (VNormal) node;
                    Class<?> clazz = obj.getClass();
//                    addErrorMessage("NormalNode" + obj.toString());
                    //            System.out.println("Fields of " + clazz.getName() + ":");
                    // Get all fields, including private ones
                    List<Field> allFields = new LinkedList<Field>();
                    while (clazz != null) {

                        Field[] fields = clazz.getDeclaredFields();
                        for (Field field : fields) {
//                            addErrorMessage(field.getName() + "fieldName");
                            // non-hidden fields, non-transient fields
                            if (!field.getName().startsWith("$") && !Modifier.isTransient(field.getModifiers()) && !isUnMutable(field)) {
                                allFields.add(field);
                            }
                        }
                        clazz = clazz.getSuperclass();  // Move to the superclass
                    }
                    // sort the fields based on its name
                    Collections.sort(allFields, new Comparator<Field>() {
                        public int compare(Field f1, Field f2) {
                            return f1.getName().compareTo(f2.getName());
                        }
                    });

                    Iterator<Field> iterator = allFields.iterator();
//                    addErrorMessage("iterator lenght" + allFields.size());
                    while (iterator.hasNext()) {
                        Field field = iterator.next();
//                        addErrorMessage(field.getName() + " visiting fieldName");
//                        addErrorMessage(String.valueOf(visited.size()));
                        try {
                            field.setAccessible(true);
                            try {
                                // Get the value of each field
                                Object value = field.get(obj);
                                if (isSimpleObject(value)) {
                                    String type;
                                    if (value != null) {
                                        type = value.getClass().getName();
                                    } else {
                                        type = "null";
                                    }
                                    myNode.addField(field.getName(), new Leaf(field.getName(), type, myNode.getDepth() + 1, "" + value, Modifier.isPublic(field.getModifiers())));
                                } else if (containsReference(value, visited)) {
                                    myNode.addField(field.getName(), new Leaf(field.getName(), value.getClass().getName(), myNode.getDepth() + 1, "visited", Modifier.isPublic(field.getModifiers())));
                                } else if (value instanceof Iterable || value.getClass().isArray()) {
                                    V newNode = new VCollection(field.getName(), value.getClass().getName(), myNode.getDepth() + 1, Modifier.isPublic(field.getModifiers()));
                                    myNode.addField(field.getName(), newNode);
                                    nextNode.add(new Pair(value, newNode));
                                    visited.add(value);
                                } else if (value instanceof Map) {
                                    V newNode = new VMap(field.getName(), value.getClass().getName(), myNode.getDepth() + 1, Modifier.isPublic(field.getModifiers()));
                                    myNode.addField(field.getName(), newNode);
                                    nextNode.add(new Pair(value, newNode));
                                    visited.add(value);
                                } else {
                                    V newNode = new VNormal(field.getName(), value.getClass().getName(), myNode.getDepth() + 1, Modifier.isPublic(field.getModifiers()));
                                    myNode.addField(field.getName(), newNode);
                                    nextNode.add(new Pair(value, newNode));
//                                    addErrorMessage("VISITED: " + field.getName());
                                    visited.add(value);
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            throw e;
                            // ignore, usually inaccessible
                        }
                    }
                } catch (Exception e) {
                    addErrorMessage("when processing normal node in processNode\n" + getStackTraceInfo(e));
                }
            }
        }
    }

    public static String getPrimitiveArrayString(Object o) {

        char c = o.getClass().getName().charAt(1);

        switch (c) {
            case 'I':
                int[] a1 = (int[]) o;
                return Arrays.toString(a1);
            case 'D':
                double[] a2 = (double[]) o;
                return Arrays.toString(a2);
            case 'F':
                float[] a3 = (float[]) o;
                return Arrays.toString(a3);
            case 'J':
                long[] a4 = (long[]) o;
                return Arrays.toString(a4);
            case 'S':
                short[] a5 = (short[]) o;
                return Arrays.toString(a5);
            case 'B':
                byte[] a6 = (byte[]) o;
                return Arrays.toString(a6);
            case 'C':
                char[] a7 = (char[]) o;
                return Arrays.toString(a7);
            case 'Z':
                boolean[] a8 = (boolean[]) o;
                return Arrays.toString(a8);
            default:
                return "unknown";
        }
    }

    public static boolean containsReference(Object o, List<Object> l) {
        for (Object obj : l) {
            if (obj == o) {
                return true;
            }
        }
        return false;
    }

    public V getRoot() {
        return root;
    }

    public static boolean isSimpleObject(Object o) {
        if (o instanceof Integer || o instanceof String || o instanceof Double || o instanceof Float || o instanceof Long || o instanceof Short || o instanceof Byte || o instanceof Character || o instanceof Boolean || o == null) {
            return true;
        }
        return false;
    }

    public void prettyPrint() {
        V node = this.root;
        System.err.println(getString(node, 2));
    }

    public String getString(V node, int depth) {
        if (node.getDepth() > depth) {
            return "";
        }
        String result = "";

        if (node instanceof Leaf) {
            Leaf myNode = (Leaf) node;
            result = "\n" + getPrefix(node.getDepth()) + "*Leaf" + "name: " + myNode.getName() + "-" + "type: " + myNode.getType() + "-" + maskNewLine(myNode.getValue(), myNode.getDepth());
        } else if (node instanceof VMap) {
            result = "\n" + result + getPrefix(node.getDepth()) + "*Map" + "name: " + node.getName() + "-type: " + node.getType() + " size: " + ((VMap) node).getKVList().size();
            for (KV entry : ((VMap) node).getKVList()) {
                if (entry.getKey().getDepth() > depth) {
                    return "";
                }
                V k = entry.getKey();
                V v = entry.getValue();
                if (k instanceof Leaf && v instanceof Leaf) {
                    result = result + "\n"+ getPrefix(v.getDepth()) + "*" + "map key: " +   maskNewLine(((Leaf) k).getValue(), v.getDepth()) + "-map value: " + maskNewLine(((Leaf) v).getValue(), v.getDepth());
                } else if (k instanceof Leaf) {
                    result = result + "\n"+ getPrefix(v.getDepth()) + "*" + "map key: " +  maskNewLine(((Leaf) k).getValue(), v.getDepth()) + "\n" + getPrefix(v.getDepth()) + "*map value: " + getString(v,depth) ;
                } else if (v instanceof Leaf) {
                    result = result + "\n"+ getPrefix(v.getDepth()) + "*" + "map key: " + getString(k,depth) + "\n" + getPrefix(v.getDepth()) + "*map value: " + maskNewLine(((Leaf) v).getValue(), v.getDepth());
                } else {
                    result = result+ "\n" + getPrefix(v.getDepth()) + "*" + "map key: " + getString(k,depth) + "\n" + getPrefix(v.getDepth()) + "*map value: " + getString(v,depth);
                }
            }
        } else if (node instanceof VCollection) {
            result = result +"\n" +  getPrefix(node.getDepth()) + "*Iterable" + "name: " + node.getName() + "-type: " + node.getType() + " size: " + ((VCollection) node).getElements().size();
            for (V v: ((VCollection) node).getElements()) {
                if (v.getDepth() > depth) {
                    return "";
                }
                if (v instanceof Leaf) {
                    result = result + "\n" + getPrefix(v.getDepth()) + "*Iterable" + "element: " + v.getType() + "-" + maskNewLine(((Leaf) v).getValue(), v.getDepth());
                } else {
                    result = result + "\n" + getPrefix(v.getDepth()) + "*Iterable" + "element: " + v.getType() + getString(v,depth);
                }
            }
        } else {
            result = result  + "\n" + getPrefix(node.getDepth()) + "*field_" + "name: " + node.getName() + "-type: " + node.getType();
            for (Map.Entry<String, V> entry: ((VNormal) node).getK_V().entrySet()) {
                V v = entry.getValue();
                if (v.getDepth() > depth) {
                    return "";
                }
                String k = entry.getKey();
                if (v instanceof Leaf) {
                    if (v.isPublic()) {
                        result = result + "\n" + getPrefix(v.getDepth()) + "*" + "f_name1: " + k + "-f_type: " + v.getType() + "-f_value: " + maskNewLine(((Leaf) v).getValue(),v.getDepth());
                    } else {
                        result = result + "\n" + getPrefix(v.getDepth()) + "*" + "f_name: " + k + "-f_type: " + v.getType() + "-f_value: " + maskNewLine(((Leaf) v).getValue(),v.getDepth());
                    }
                } else {
                    if (v.isPublic()) {
                        result = result + "\n" + getPrefix(v.getDepth()) + "*" + "f_name1: " + k + "-f_type: " + v.getType() + getString(v,depth);
                    } else {
                        result = result + "\n" + getPrefix(v.getDepth()) + "*" + "f_name: " + k + "-f_type: " + v.getType() + getString(v,depth);
                    }
                }
            }
        }
        return result;
    }

    public static String getPrefix(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    /**
     * mask the new line character with proper indentation;
     * if the string starts with a space character, then the added "." after indentation will work.
     * @param s
     * @param depth
     * @return
     */
    public static String maskNewLine(String s, int depth) {
        if (s == null) return "null";
        if (s.length() > 10000) return "too long";
        if (containsSuspicious(s)) return "suspicious";

        String prefix = getPrefix(depth) + " ";
        return s.replace("\n", "\n" + prefix + '.');
    }

    public static boolean isUnMutable(Field field) {
        boolean isFinal = Modifier.isFinal(field.getModifiers());
        boolean isPrimitive = field.getType().isPrimitive();
        boolean isString = field.getType().equals(String.class);

        // Check if the field is final and either primitive or a String
        if (isFinal && (isPrimitive || isString)) {
            return true;
        }
        return false;
    }

    public static boolean containsSuspicious(String s) {

        // If we suspect an Object hashCode not use this, as it may lead to flaky tests
        if(addressPattern.matcher(s).matches())
            return true;
        if (s.toLowerCase().contains("mockito"))
            return true;
        // this is the file path of my laptop
        if (s.toLowerCase().contains("testobserver"))
            return true;
        if (s.toLowerCase().contains("lambda") || s.toLowerCase().contains("$$") || s.toLowerCase().contains("/0x"))
            return true;
        // "superhang" is the username in absolute paths on Hang's personal
        // machine (e.g. /Users/superhang/...); hard-coded on purpose to scrub
        // those local paths out of observed state. Only relevant when run there.
        if (s.toLowerCase().contains("superhang"))
            return true;

        return false;
    }


}

