package org.instrumentation.group0instrumentor.states;

import org.helper.states.Loc;
import org.helper.states.StateItem;
import org.helper.states.type;
import org.junit.jupiter.api.Test;
import org.general.BaseExampleClass;
import org.general.ExampleClass;
import org.junit.jupiter.api.Timeout;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StateItemTest {
    @Test
    public void testGetObserversSimpleObject() {
        Object o = new Object();
        StateItem s = new StateItem(new Loc("",""), type.Object, "content", o);
        Map<String, String> m = s.recordObserverInfo();
        assertEquals(1, m.size());
        assertEquals("false", m.get("public final native java.lang.Class java.lang.Object.getClass()"));
    }

    @Test
    public void testGetObserversExample1() {
        BaseExampleClass b = new ExampleClass();
        StateItem s = new StateItem(new Loc("",""), type.Object, "content", b);
        Map<String, String> m = s.recordObserverInfo();
        assertEquals(8, m.keySet().size());
        assertEquals("x", m.get("public java.lang.String org.general.ExampleClass.getX()") );
        assertEquals("Ha HY",m.get("public java.lang.String org.general.ExampleClass.getName()"));
        assertEquals("false", m.get("public final native java.lang.Class java.lang.Object.getClass()"));
        assertEquals("11", m.get("public int org.general.ExampleClass.getOne()"));
        assertEquals("true", m.get("public java.lang.Object org.general.ExampleClass.getNull()"));
        assertEquals("Y",m.get("public default java.lang.String org.general.MyInterface.getY()"));
        assertEquals("0", m.get("public int org.general.ExampleClass.getNumber()"));
    }
}