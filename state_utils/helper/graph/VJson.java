package org.helper.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class VJson {
    static final ObjectMapper MAPPER = new ObjectMapper();

    /** One-shot: returns a standalone JSON tree for a single root. */
    public static JsonNode toJsonTree(V root, int maxDepth) {
        return toJsonNode(root, maxDepth, new IdentityHashMap<V, String>(), new AtomicInteger(1));
    }

    /** Pretty string if you want it. */
    public static String toJsonString(V root, int maxDepth) throws JsonProcessingException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(toJsonTree(root, maxDepth));
    }

    /** Session: reuse IDs/seen across many roots (lets metas cross-reference the same graph). */
    public static final class Session {
        private final IdentityHashMap<V, String> seen = new IdentityHashMap<V, String>();
        private final AtomicInteger nextId = new AtomicInteger(1);
        public JsonNode toJsonTree(V root, int maxDepth) {
            return toJsonNode(root, maxDepth, seen, nextId);
        }
    }

    // --- existing serializer core (unchanged) ---
    private static ObjectNode toJsonNode(
            V node, int maxDepth, IdentityHashMap<V, String> seen, AtomicInteger nextId) {

        if (node.getDepth() > maxDepth) {
            ObjectNode trunc = MAPPER.createObjectNode();
            trunc.put("truncated", true);
            trunc.put("depth", node.getDepth());
            trunc.put("kind", kindOf(node));
            trunc.put("name", node.getName());
            trunc.put("javaType", node.getType());
//            trunc.put("ordinal", node.get);
            return trunc;
        }

        String knownId = seen.get(node);
        if (knownId != null) {
            ObjectNode ref = MAPPER.createObjectNode();
            ref.put("$ref", knownId);
            return ref;
        }
        String id = "n" + nextId.getAndIncrement();
        seen.put(node, id);

        ObjectNode out = MAPPER.createObjectNode();
        out.put("$id", id);
        out.put("kind", kindOf(node));
        if (node.getName() != null) out.put("name", node.getName());
        if (node.getType() != null) out.put("javaType", node.getType());
        out.put("depth", node.getDepth());
        if (node.isPublic()) out.put("visibility", "public");

        if (node instanceof Leaf) {
            out.put("value", ((Leaf) node).getValue());
            return out;
        }
        if (node instanceof VMap) {
            ArrayNode entries = MAPPER.createArrayNode();
            for (KV kv : ((VMap) node).getKVList()) {
                ObjectNode e = MAPPER.createObjectNode();
                e.set("key", toJsonNode(kv.getKey(), maxDepth, seen, nextId));
                e.set("value", toJsonNode(kv.getValue(), maxDepth, seen, nextId));
                entries.add(e);
            }
            out.set("entries", entries);
            out.put("size", ((VMap) node).getKVList().size());
            return out;
        }
        if (node instanceof VCollection) {
            ArrayNode elements = MAPPER.createArrayNode();
            for (V v : ((VCollection) node).getElements()) {
                elements.add(toJsonNode(v, maxDepth, seen, nextId));
            }
            out.set("elements", elements);
            out.put("size", ((VCollection) node).getElements().size());
            return out;
        }

        ObjectNode fields = MAPPER.createObjectNode();
        for (Map.Entry<String, V> e : ((VNormal) node).getK_V().entrySet()) {
            fields.set(e.getKey(), toJsonNode(e.getValue(), maxDepth, seen, nextId));
        }
        out.set("fields", fields);
        return out;
    }

    private static String kindOf(V node) {
        if (node instanceof Leaf) return "leaf";
        if (node instanceof VMap) return "map";
        if (node instanceof VCollection) return "collection";
        return "object";
    }
}
