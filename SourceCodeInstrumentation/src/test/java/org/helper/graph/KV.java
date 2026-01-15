package org.helper.graph;

public class KV {

    private V key;
    private V value;

    public KV(V key, V value) {
        this.key = key;
        this.value = value;
    }

    public V getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

}
