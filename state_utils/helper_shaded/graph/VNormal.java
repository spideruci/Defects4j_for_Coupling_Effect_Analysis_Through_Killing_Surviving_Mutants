package org.helper.graph;

import java.util.LinkedHashMap;
import java.util.Map;

public class VNormal extends V{

    private Map<String, V> K_V = new LinkedHashMap<String, V>();

    public VNormal(String name, String type, int depth, boolean isPublic) {
        super(name, type, depth, isPublic);
        this.K_V = K_V;
    }

    public void addField(String name, V value) {
        K_V.put(name, value);
    }

    public Map<String, V> getK_V() {
        return K_V;
    }

}
