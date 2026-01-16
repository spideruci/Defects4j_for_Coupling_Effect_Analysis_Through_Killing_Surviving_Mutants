package org.helper.graph;

abstract public class V {

    protected String type; // type of the variable, with "null" as an exception;

    protected String name; // name of the variable, sometimes null if it is related to iterable or map

    protected int depth;

    protected boolean isPublic = false;

    protected int ordinal = 0;

//    public V(String name, String type, int depth) {
//        this.name = name;
//        this.type = type;
//        this.depth = depth;
//    }

    public V(String name, String type, int depth, boolean isPublic) {
        this.name = name;
        this.type = type;
        this.depth = depth;
        this.isPublic = isPublic;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getDepth() {
        return depth;
    }

    public String getSpace () {
        String result = "";
        for (int i = 0; i < depth; i++) {
            result += "  ";
        }
        return result;
    }

    public boolean isPublic() {
        return isPublic;
    }

}
