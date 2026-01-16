// Instrumented at 2025-11-28 09:38:36
package com.fasterxml.jackson.databind.objectid;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import java.util.*;

public class TestAbstractWithObjectId extends BaseMapTest {

    interface BaseInterface {
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class BaseInterfaceImpl implements BaseInterface {

        @JsonProperty
        private List<BaseInterfaceImpl> myInstances = new ArrayList<BaseInterfaceImpl>();

        void addInstance(BaseInterfaceImpl instance) {
            myInstances.add(instance);
        }
    }

    static class ListWrapper<T extends BaseInterface> {

        @JsonProperty
        private List<T> myList = new ArrayList<T>();

        void add(T t) {
            myList.add(t);
        }

        int size() {
            return myList.size();
        }
    }

    public void testIssue877() throws Exception {
        ObjectMapper __ins_v1 = null;
        // make two instances
        BaseInterfaceImpl one = new BaseInterfaceImpl();
        BaseInterfaceImpl two = new BaseInterfaceImpl();
        // add them to each other's list to show identify info being used
        one.addInstance(two);
        two.addInstance(one);
        // make a typed version of the list and add the 2 instances to it
        ListWrapper<BaseInterfaceImpl> myList = new ListWrapper<BaseInterfaceImpl>();
        myList.add(one);
        myList.add(two);
        __ins_v1 = new ObjectMapper();
        // make an object mapper that will add class info in so deserialisation works
        ObjectMapper om = __ins_v1;
        om.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        // write and print the JSON
        String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(myList);
        ListWrapper<BaseInterfaceImpl> result;
        result = om.readValue(json, new TypeReference<ListWrapper<BaseInterfaceImpl>>() {
        });
        assertNotNull(result);
        // see what we get back
        assertEquals(2, result.size());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1380_", __ins_v1);
    }
}
