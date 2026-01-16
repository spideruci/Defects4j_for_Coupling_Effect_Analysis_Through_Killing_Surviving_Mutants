// Instrumented at 2025-12-13 14:00:52
package com.fasterxml.jackson.databind.ser;

import java.util.concurrent.atomic.AtomicReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;

public class TestSerializerProvider extends com.fasterxml.jackson.databind.BaseMapTest {

    static class MyBean {

        public int getX() {
            return 3;
        }
    }

    static class NoPropsBean {
    }

    public void testFindExplicit() throws JsonMappingException {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerFactory f = new BeanSerializerFactory(null);
        DefaultSerializerProvider prov = new DefaultSerializerProvider.Impl().createInstance(config, f);
        // Should have working default key and null key serializers
        assertNotNull(prov.findKeySerializer(mapper.constructType(String.class), null));
        assertNotNull(prov.getDefaultNullKeySerializer());
        assertNotNull(prov.getDefaultNullValueSerializer());
        // as well as 'unknown type' one (throws exception)
        assertNotNull(prov.getUnknownTypeSerializer(getClass()));
        assertTrue(prov.createInstance(config, f).hasSerializerFor(String.class, null));
        // call twice to verify it'll be cached (second code path)
        assertTrue(prov.createInstance(config, f).hasSerializerFor(String.class, null));
        assertTrue(prov.createInstance(config, f).hasSerializerFor(MyBean.class, null));
        assertTrue(prov.createInstance(config, f).hasSerializerFor(MyBean.class, null));
        // And then some negative testing
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertFalse(prov.createInstance(config, f).hasSerializerFor(NoPropsBean.class, cause));
        Throwable t = cause.get();
        // no actual exception: just fails since there are no properties
        assertNull(t);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_3365_18", __ins_v1);
    }
}
