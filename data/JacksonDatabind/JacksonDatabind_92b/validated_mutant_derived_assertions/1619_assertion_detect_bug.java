// Instrumented at 2025-12-01 00:17:15
package com.fasterxml.jackson.databind.type;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

// Tests for [databind#1456]: resolution using methods deprecated
// in 2.7, but used to work in 2.6
public class DeprecatedConstructType1456Test extends BaseMapTest {

    public static class BaseController<Entity extends BaseEntity> {

        public void process(Entity entity) {
        }
    }

    public static class ImplController extends BaseController<ImplEntity> {
    }

    public static class BaseEntity {
    }

    public static class ImplEntity extends BaseEntity {
    }

    private final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings("deprecation")
    public void testGenericResolutionUsingDeprecated() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        Method proceed = BaseController.class.getMethod("process", BaseEntity.class);
        Type entityType = proceed.getGenericParameterTypes()[0];
        __ins_v1 = MAPPER;
        JavaType resolvedType = __ins_v1.getTypeFactory().constructType(entityType, ImplController.class);
        assertEquals(ImplEntity.class, resolvedType.getRawClass());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_344_32", __ins_v1);
    }

    // and this is how new code should resolve types if at all possible
    public void testGenericParameterViaClass() throws Exception {
        BeanDescription desc = MAPPER.getDeserializationConfig().introspect(MAPPER.constructType(ImplController.class));
        AnnotatedClass ac = desc.getClassInfo();
        AnnotatedMethod m = ac.findMethod("process", new Class<?>[] { BaseEntity.class });
        assertNotNull(m);
        assertEquals(1, m.getParameterCount());
        AnnotatedParameter param = m.getParameter(0);
        assertEquals(ImplEntity.class, param.getType().getRawClass());
    }
}
