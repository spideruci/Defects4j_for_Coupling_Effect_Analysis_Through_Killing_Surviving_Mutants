package org.helper;

import org.helper.states.Loc;
import org.helper.states.StateItem;
import org.helper.states.type;

import static org.helper.TestExtension.states;

public class InstrumentationUtils {

    public static int processInteger(int i, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.I, "" + i, null));
        return i;
    }

    public static double processDouble(double d, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.D, "" + d, null));
        return d;
    }

    public static boolean processBoolean(boolean b, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.Z, "" + b, null));
        return b;
    }

    public static char processChar(char c, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.C, "" + c, null));
        return c;
    }

    public static byte processByte(byte b, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.B, "" + b, null));
        return b;
    }

    public static short processShort(short s, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.S, "" + s, null));
        return s;
    }

    public static long processLong(long l, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.J, "" + l, null));
        return l;
    }

    public static float processFloat(float f, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.F, "" + f, null));
        return f;
    }

    public static String processString(String s, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.String, s, null));
        return s;
    }

    public static void processObject(Object o, String line, String source) {
        states.addState(new StateItem(new Loc(line, source), type.Object, "Object", o));
    }
}
