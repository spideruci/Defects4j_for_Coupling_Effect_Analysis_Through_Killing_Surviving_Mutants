// Instrumented at 2025-12-01 00:17:06
package com.fasterxml.jackson.databind.interop;

import java.lang.reflect.*;
import com.fasterxml.jackson.databind.*;

// mostly for [Issue#57]
public class TestJDKProxy extends BaseMapTest {

    final ObjectMapper MAPPER = new ObjectMapper();

    public interface IPlanet {

        String getName();

        String setName(String s);
    }

    // bit silly example; usually wouldn't implement interface (no need to proxy if it did)
    static class Planet implements IPlanet {

        private String name;

        public Planet() {
        }

        public Planet(String s) {
            name = s;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String setName(String iName) {
            name = iName;
            return name;
        }
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */
    public void testSimple() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        IPlanet input = getProxy(IPlanet.class, new Planet("Foo"));
        __ins_v1 = MAPPER;
        String json = __ins_v1.writeValueAsString(input);
        assertEquals("{\"name\":\"Foo\"}", json);
        // and just for good measure
        Planet output = MAPPER.readValue(json, Planet.class);
        assertEquals("Foo", output.getName());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_771_32", __ins_v1);
    }

    /*
    /********************************************************
    /* Helper methods
    /********************************************************
     */
    public static <T> T getProxy(Class<T> type, Object obj) {
        class ProxyUtil implements InvocationHandler {

            Object _obj;

            public ProxyUtil(Object o) {
                _obj = o;
            }

            @Override
            public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                Object result = null;
                result = m.invoke(_obj, args);
                return result;
            }
        }
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, new ProxyUtil(obj));
        return proxy;
    }
}
