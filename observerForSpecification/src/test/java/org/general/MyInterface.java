package org.general;

public interface MyInterface {
    String getX();

    default String getY() {
        return "Y";
    }
}
