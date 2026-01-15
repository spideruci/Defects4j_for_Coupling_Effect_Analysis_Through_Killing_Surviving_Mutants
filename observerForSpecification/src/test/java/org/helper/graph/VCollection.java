package org.helper.graph;

import java.util.LinkedList;
import java.util.List;

public class VCollection extends V{

    private List<V> elements = new LinkedList<V>();

    public VCollection(String name, String type, int depth, boolean isPublic) {
        super(name, type, depth, isPublic);
    }

    public void addNode(V node) {
        elements.add(node);
    }

    public List<V> getElements() {
        return elements;
    }

}
