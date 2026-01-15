package org.general;

import java.util.ArrayList;
import java.util.List;

public class ObjectFieldExample {

    private int intValue = 3;
    public short shortValue = 10;
    private String stringValue = "Ha HY";
    private Object object = new Object();
    public static BaseExampleClass e = new ExampleClass();
    private ExampleClass nullObject = null;
    public List<BaseExampleClass> list = new ArrayList<BaseExampleClass>();

    public ObjectFieldExample() {
        list.add(new ExampleClass());
        list.add(new ExampleClass());
    }
}
