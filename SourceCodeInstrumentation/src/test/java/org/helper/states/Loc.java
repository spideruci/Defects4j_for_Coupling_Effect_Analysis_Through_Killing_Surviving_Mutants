package org.helper.states;

import java.lang.reflect.Method;

public class Loc {
    private String line;
    private String source;

    public Loc(String line, Method source) {
        this.line = line;
        this.source = source.toString();
    }

    public Loc(String line, String source) {
        this.line = line;
        this.source = source.toString();
    }

    public String getLine() {
        return line;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Loc{" +
                "line=" + line +
                ", source='" + source + '\'' +
                '}';
    }
}
