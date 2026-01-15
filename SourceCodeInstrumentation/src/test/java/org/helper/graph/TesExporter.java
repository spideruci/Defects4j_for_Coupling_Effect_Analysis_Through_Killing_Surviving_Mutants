package org.helper.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.helper.states.StateItem;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class TesExporter {

    /**
     * Export one unified JSON doc containing:
     *  - kind: "state" metas from TesState.states
     *  - kind: "this" meta if Arguments.dumpThis
     *  - kind: "static" metas if Arguments.dumpStatic
     *
     * Cross-meta dedup is enabled via a single VJson.Session and a domain-object -> V-root cache.
     */
    public static String exportRun(
            List<StateItem> states,
            boolean dumpThis,
            Object thisObj,
            boolean dumpStatic,
            List<String> staticFieldSpecs, // e.g., "com.acme.C::FIELD" or the "A-B" format you already have
            int graphBuildDepth,            // your Graph(...) constructor depth
            int maxDepth                    // JSON maxDepth cutoff
    ) throws JsonProcessingException {

        // Top-level doc
        ObjectNode doc = VJson.MAPPER.createObjectNode();
        doc.put("schema", "tes-meta-graph@1");
        doc.put("graphBuildDepth", graphBuildDepth);
        doc.put("maxDepth", maxDepth);

        ArrayNode metas = VJson.MAPPER.createArrayNode();
        doc.set("metas", metas);

        // One session across all metas for $id/$ref stability
        VJson.Session session = new VJson.Session();
        // Cache domain object -> V root so identity matches even if we rebuild Graph wrappers
        IdentityHashMap<Object, V> rootCache = new IdentityHashMap<Object, V>();
        AtomicInteger idx = new AtomicInteger(0);

        // ---- A) state metas (your original TesState.states) ----
        for (StateItem s : states) {
            ObjectNode m = baseMeta(idx.getAndIncrement(), "state");
            m.put("line", s.getLine());
            System.err.println(s.getSource());
            m.put("source", s.getSource().split("-")[0]);
            m.put("owner", s.getSource().split("-")[1]);
            m.put("ordinal", s.getOrdinal());
            // for array, it can be array-java/lang/String
            if (s.getSource().split("-")[0].equals("local")) {
                m.put("localType", s.getSource().split("-")[2]);
            }
            if (s.getSource().split("-").length == 4) {
                m.put("name", s.getSource().split("-")[2]);
                String descriptor = s.getSource().split("-")[3]; // e.g. (Ljava/lang/String;)I
                String returnDescriptor = descriptor.substring(descriptor.lastIndexOf(')') + 1);
                String returnType = mapDescriptorToType(returnDescriptor);
                m.put("returnType", returnType);
            }




            m.put("content", maskNewLine(s.getContent(), 1));

            Object obj = s.getObject();
            attachGraphIfNonNull(m, obj, graphBuildDepth, maxDepth, session, rootCache);
            metas.add(m);
        }

        // ---- B) "this" meta (if requested) ----
        if (dumpThis) {
            ObjectNode m = baseMeta(idx.getAndIncrement(), "this");
            // You created: new Loc("this","this") in your snippet; reflect that here if useful
            m.put("line", "this");
            m.put("source", "this");

            m.put("content", ""); // or add anything you want logged here

            attachGraphIfNonNull(m, thisObj, graphBuildDepth, maxDepth, session, rootCache);
            metas.add(m);
        }

        // ---- C) static field metas (if requested) ----
        if (dumpStatic) {
            for (String spec : staticFieldSpecs) {
                // You currently parse by "-", adapt as needed. Here's a tolerant parser:
                String className;
                String fieldName;
                String[] p = spec.split("-");
                className = p[0];
                fieldName = p[1];

                ObjectNode m = baseMeta(idx.getAndIncrement(), "static");
                m.put("className", className);
                m.put("fieldName", fieldName);

                try {
                    Class<?> clazz = Class.forName(className);
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value;
                    try {
                        value = field.get(null); // static
                    } catch (NullPointerException npe) {
                        value = null;
                    }

                    if (value == null) {
                        m.put("valueNull", true);
                    } else {
                        m.put("valueNull", false);
                        attachGraphIfNonNull(m, value, graphBuildDepth, maxDepth, session, rootCache);
                    }
                } catch (ClassNotFoundException e) {
                    // don’t throw—record the error inline so the doc stays complete
                    m.put("error", e.toString());
                } catch (NoSuchFieldException e) {
                    m.put("error", e.toString());
                } catch (IllegalArgumentException e) {
                    m.put("error", e.toString());
                } catch (IllegalAccessException e) {
                    m.put("error", e.toString());
                }
                metas.add(m);
            }
        }


        return VJson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(doc);
    }

    // ---------- helpers ----------

    private static ObjectNode baseMeta(int index, String kind) {
        ObjectNode m = VJson.MAPPER.createObjectNode();
        m.put("index", index);
        m.put("kind", kind);
        return m;
    }

    /**
     * Attach a "graph" field by serializing the object's V-root.
     * Uses per-run session + domain-object cache for stable $id/$ref.
     */
    private static void attachGraphIfNonNull(
            ObjectNode meta,
            Object domainObject,
            int graphBuildDepth,
            int maxDepth,
            VJson.Session session,
            IdentityHashMap<Object, V> rootCache
    ) {
        if (domainObject == null) return;
        try {
            V root = rootCache.get(domainObject);
            if (root == null) {
                root = new Graph(domainObject, graphBuildDepth).getRoot();
                rootCache.put(domainObject, root);
            }
//            V root = rootCache.computeIfAbsent(domainObject, o -> new Graph(o, graphBuildDepth).getRoot());
            JsonNode graphTree = session.toJsonTree(root, maxDepth);
            meta.set("graph", graphTree);
        } catch (Exception e) {
            meta.put("graphError", e.toString());
        }
    }

    // reuse your existing masking policy (or drop it—JSON escaping already handles newlines)
    private static String maskNewLine(String s, int depth) {
        return s == null ? null : s; // plug in your real function
    }

    private static String mapDescriptorToType(String desc) {
        if ("V".equals(desc)) {
            return "void";
        } else if ("I".equals(desc)) {
            return "int";
        } else if ("J".equals(desc)) {
            return "long";
        } else if ("D".equals(desc)) {
            return "double";
        } else if ("F".equals(desc)) {
            return "float";
        } else if ("B".equals(desc)) {
            return "byte";
        } else if ("C".equals(desc)) {
            return "char";
        } else if ("S".equals(desc)) {
            return "short";
        } else if ("Z".equals(desc)) {
            return "boolean";
        } else {
            if (desc.startsWith("L") && desc.endsWith(";")) {
                // object type: strip L...;
                return desc.substring(1, desc.length() - 1).replace('/', '.');
            } else if (desc.startsWith("[")) {
                // array type: recurse on element
                return mapDescriptorToType(desc.substring(1)) + "[]";
            } else {
                return desc; // fallback: raw descriptor
            }
        }
    }

    public static String exportVar(
            StateItem s,
            int graphBuildDepth,            // your Graph(...) constructor depth
            int maxDepth                    // JSON maxDepth cutoff
    ) throws JsonProcessingException {

        // Top-level doc
        ObjectNode doc = VJson.MAPPER.createObjectNode();
        doc.put("schema", "tes-meta-graph@1");
        doc.put("graphBuildDepth", graphBuildDepth);
        doc.put("maxDepth", maxDepth);

        ArrayNode metas = VJson.MAPPER.createArrayNode();
        doc.set("metas", metas);

        // One session across all metas for $id/$ref stability
        VJson.Session session = new VJson.Session();
        // Cache domain object -> V root so identity matches even if we rebuild Graph wrappers
        IdentityHashMap<Object, V> rootCache = new IdentityHashMap<Object, V>();
        AtomicInteger idx = new AtomicInteger(0);

        // ---- A) state metas (your original TesState.states) ----

        ObjectNode m = baseMeta(idx.getAndIncrement(), "state");

        Object obj = s.getObject();
        attachGraphIfNonNull(m, obj, graphBuildDepth, maxDepth, session, rootCache);
        metas.add(m);
        return VJson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(doc);
    }


}
