package org.helper.graph;

import java.util.LinkedList;
import java.util.List;

public class VMap extends V{

    private List<KV> kvList = new LinkedList<KV>();

    public VMap(String name, String type, int depth, boolean isPublic) {
        super(name, type, depth, isPublic);
    }

    public void addKVPair(V v1, V v2) {
        kvList.add(new KV(v1,v2));
    }

    public List<KV> getKVList() {
        return kvList;
    }
}
