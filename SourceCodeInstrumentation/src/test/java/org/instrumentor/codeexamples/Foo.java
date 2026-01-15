package org.instrumentor.codeexamples;

public class Foo {

    public Person person = new Person("John", 30);

    public Foo foo() { return this; }

    public void bar() {
        System.out.println("bar() called");
    }

    public void usePersonAsArgument(Person person) {

    }

}
