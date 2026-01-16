package org.helper;

import java.util.List;

public class Spec {
    public String source;
    public String owner;
    public String name;
    public String returnType;
    public int ordinal;
    public String readable_access;
    public List<Object> python_access;  // because it can mix strings and ints
    public String test_name;
    public int line_number;
    public String simple_class_name;

    @Override
    public String toString() {
        return "Spec{" +
                "source='" + source + '\'' +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", returnType='" + returnType + '\'' +
                ", ordinal=" + ordinal +
                ", readable_access='" + readable_access + '\'' +
                ", python_access=" + python_access +
                ", test_name='" + test_name + '\'' +
                ", line_number=" + line_number +
                '}';
    }
}

