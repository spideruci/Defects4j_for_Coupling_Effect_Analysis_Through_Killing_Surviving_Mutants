package org.helper.graph;

public class Leaf extends V {

    private String value;

//    public Leaf(String name, String type, int depth, String value, boolean isPublic) {
//        super(name, type, depth, isPublic);
//        this.value = value;
//    }

    public Leaf(String name, String type, int depth, String value, boolean isPublic) {
        super(name, type, depth, isPublic);
        this.value = value;
    }
//
//    public static Leaf getLeafNode(Object obj) {
//        return new Leaf("root", obj.getClass().getName(), 0, obj.toString());
//    }

    public String getValue() {
        return value;
    }

}
