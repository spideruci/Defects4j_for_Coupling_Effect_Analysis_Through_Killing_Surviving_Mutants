package org.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group0Example {

    int x = 0;

    String y = "jdlksa";

    List<ExampleClass> z = new ArrayList<ExampleClass>();

    Map<String, List<ExampleClass>> map = new HashMap<String, List<ExampleClass>>();

    public Group0Example() {
        z.add(new ExampleClass());
        map.put("dklsaj", z);
        map.put("21",z);
        map.put("empty", new ArrayList<ExampleClass>());
    }


    public int getInteger() {
        return 0;
    }

    public float getFloat() {
        return 1.2f;
    }

    public double getDouble() {
        return 2.3;
    }

    public long getLong() {
        return 3L;
    }

    public short getShort() {
        return 4;
    }

    public byte getByte() {
        return 5;
    }

    public char getChar() {
        return '6';
    }

    public boolean getBoolean() {
        return false;
    }

    private String getString() {
        return "4345";
    }

    public Object getNullObject() {
        return null;
    }

    public Object getNonNullObject() {
        return new Object();
    }
}
