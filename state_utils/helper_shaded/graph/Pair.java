package org.helper.graph;

public class Pair {

    private Object obj;
    private V node;

    public Pair (Object obj, V node) {
        this.obj = obj;
        this.node = node;
    }

    public Object getObj() {
        return obj;
    }

    public V getNode() {
        return node;
    }


}
