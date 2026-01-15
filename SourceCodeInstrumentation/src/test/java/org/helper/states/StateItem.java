package org.helper.states;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class StateItem {

    private Loc loc;

    private type t;

    private String content;

    // how to describe the observer: method.toString();
    private Map<String,String> observers;

    private Object object;

    private int ordinal;

    /**
     * parse the state information from object
     * @param loc
     * @param t
     * @param content
     * @param o, o is checked to be non-null before the method is called;
     */
    public StateItem(Loc loc, type t, String content, Object o, int ordinal) {
        this.loc = loc;
        this.t = t;
        this.content = content;
        this.object = o;
        this.observers = new HashMap<String,String>();
        this.ordinal = ordinal;
    }

    /**
     * get the object's states based on observer methods; The method is invoked iff object e is not null;
     * @return
     */
    public Map<String, String> recordObserverInfo() {

        Object e = this.object;
        if (e == null) {
            return null;
        }

        return getStatesThroughGetters(e);
    }

    public static Map<String, String> getStatesThroughFields(Object e) {
        return null;
    }

    public static Map<String,String> getStatesThroughGetters(Object e) {
        Map<String, String> observers = new HashMap<String,String>();
        Class<?> objClass = e.getClass();

        // only set the modifier of a class to public if it is not-public inner class

        // but for methods, only invoke private observers
        for (Method method : objClass.getMethods()) {
            if (isSimpleGetterMethod(method) && !isReturnPrimitiveOrString(method)) {
                try {
                    Method m = objClass.getMethod(method.getName(), new Class<?>[0]); // no-arg getter
                    if (!m.isAccessible()) {
                        m.setAccessible(true);
                    }
                    Object val = m.invoke(e, new Object[0]); // invoke no-arg
                    observers.put(method.toString(), String.valueOf(val == null));
                } catch (NoSuchMethodException ex) {
                    System.err.println("No such method in getObserverMethod when getting states for object");
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    System.err.println("Illegal access in getObserverMethod when getting states for object");
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    System.err.println("Invocation target exception in getObserverMethod when getting states for object");
                    throw new RuntimeException(ex);
                }
//                try {
//                    Method m = objClass.getMethod(method.getName());
//                    if (!m.isAccessible()) {
//                        m.setAccessible(true); // Set the method accessible if it's not
//                    }
//                    observers.put(method.toString(), "" + (((Object) m.invoke(e)) == null));
//                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
//                    System.err.println("exception in getObserverMethod when getting states for object");
//                    throw new RuntimeException(ex);
//                }
            } else {
                try {
                    // no-arg getter
                    Method m = objClass.getMethod(method.getName(), new Class[0]);
                    if (!m.isAccessible()) {
                        m.setAccessible(true);
                    }
                    Object ret = m.invoke(e, new Object[0]); // invoke no-arg
                    String temp = String.valueOf(ret);       // "null" if ret == null
                    observers.put(method.toString(), temp);
                } catch (NoSuchMethodException ex) {
                    System.err.println("No such method in getObserverMethod when getting states for primitive or string-typed values");
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    System.err.println("Illegal access in getObserverMethod when getting states for primitive or string-typed values");
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    System.err.println("Invocation target exception in getObserverMethod when getting states for primitive or string-typed values");
                    throw new RuntimeException(ex);
                }



//                if (isSimpleGetterMethod(method) && isReturnPrimitiveOrString(method)) {
//                    try {
//                        Method m = objClass.getMethod(method.getName());
//                        if (!m.isAccessible()) {
//                            m.setAccessible(true); // Set the method accessible if it's not
//                        }
//                        String temp = "" + m.invoke(e);
//                        observers.put(method.toString(), temp);
//                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
//                        System.err.println("exception in getObserverMethod when getting states for primitive or string-typed values");
//                        throw new RuntimeException(ex);
//                    }
//
//                }
            }
        }
        return observers;
    }

    public static void prettyPrintMap(Map<String, String> m) {
        for (Map.Entry<String, String> entry : m.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    // Helper method to determine if a given method is a getter
    public static boolean isSimpleGetterMethod(Method method) {

        // Getter methods must be public or protected
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers)) {
            return false;
        }
        // Getter methods must not take any parameters
        if (method.getParameterTypes().length != 0) return false;
        // Getter methods typically start with "get"
        if (method.getName().startsWith("get") && method.getName().length() > 3) {
            return true;
        }
        // or start with "is" (for booleans)
        if (method.getName().startsWith("is") && method.getName().length() > 2 &&
                method.getReturnType().equals(boolean.class)) {
            return true;
        }
        return false;
    }

    public static boolean isReturnPrimitiveOrString(Method method) {
        return !method.getReturnType().getName().equals("void") &&
                (method.getReturnType().isPrimitive() || method.getReturnType().equals(String.class));
    }

    public String getLine() {
        return loc.getLine();
    }

    public Loc getLoc() {
        return loc;
    }

    public type getType() {
        return t;
    }

    public String getContent() {
        return content;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public Map<String, String> getObservers() {
        return observers;
    }

    public String getSource() {
        return loc.getSource();
    }

    public Object getObject() {
        return object;
    }
}

