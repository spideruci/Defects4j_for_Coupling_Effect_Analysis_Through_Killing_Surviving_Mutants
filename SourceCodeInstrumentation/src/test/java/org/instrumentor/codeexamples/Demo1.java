package org.instrumentor.codeexamples;
import org.junit.jupiter.api.Test;

public class Demo1 {
    @Test
    public void test() {
        new Foo().foo().foo().foo().bar(); new Foo();

        new Foo().foo().foo();
    }


    @Test
    public void test2() {
        new Foo().foo().bar();
    }

    @Test
    public void test3() {
        Foo foo = new Foo().foo();
        Person person; foo.usePersonAsArgument(foo.person);
        // the AST should first look for  XX.person; if not then look for person in the getField Scope
    }


    private Person person = new Person("Jim", 25);
    @Test
    public void test4() {
        Foo foo = new Foo().foo();
        foo.usePersonAsArgument(person); foo.usePersonAsArgument(person);
    }

    @Test
    public void test5() {
        org.instrumentor.codeexamples.Foo foo = new Foo().foo();
        foo.usePersonAsArgument(person); foo.usePersonAsArgument(person);
    }

    @Test
    public void test6() {
        Foo foo;
        foo = new Foo().foo();
        foo.usePersonAsArgument(person); foo.usePersonAsArgument(person);
    }

    @Test
    public void test7() {
        Foo foo;
        foo = new Foo().foo();
        person = new Person("Jim", 25);
        foo.usePersonAsArgument(person); foo.usePersonAsArgument(person);
    }

    @Test
    public void testTryCatch8() {
        Foo foo;
        foo = new Foo().foo();
        try {
            person = new Person("Jim", 25);
            foo.usePersonAsArgument(person); foo.usePersonAsArgument(person);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNewLineReturn() {
        Foo foo;
        foo = new Foo().
                foo();
        try {
            person = new Person("Jim", 25);
            foo.usePersonAsArgument(person); foo.usePersonAsArgument(person);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test10() {
        Foo foo =
                new Foo()
                        .foo();
        foo.usePersonAsArgument(person);
        foo.usePersonAsArgument(person);
        // the AST should first look for  XX.person; if not then look for person in the getField Scope
    }

    @Test
    public void test12() {
        new Foo().foo().foo().foo().bar(); new Foo();
        new Foo().foo().foo();
        for (int i=0; i<5; i++) {
            new Foo().foo().foo().bar();
        }
    }

    @Test
    public void test13() {
        for (int i = 5; i < 10; i++) {
            new Foo().foo().foo().foo().bar(); new Foo();
        }
        new Foo().foo().foo();
    }


    @Test
    public void test14() {
        Foo foo = new Foo().foo();
        for (int i = 0; i < 3; i++) {
            Person person;
            foo.usePersonAsArgument(foo.person);
        }

        // the AST should first look for  XX.person; if not then look for person in the getField Scope
    }


    @Test
    public void test15() {
        for (int i = 0; i < 3; i++) {
            Foo foo = new Foo().foo();
            foo.usePersonAsArgument(person);
            Person person = new Person("Jim", 25);
        }

    }
    @Test
    public void test16() {
        Foo foo;
        for (int i = 0; i < 3; i++) {
            foo = new Foo().foo();
            foo.usePersonAsArgument(person);
            foo.usePersonAsArgument(person);
        }
    }

    @Test
    public void test17() {
        Foo foo;
        for (int i = 0; i < 3; i++) {
            foo = new Foo().foo();
            this.person = new Person("Jim", 25);
            foo.usePersonAsArgument(person);
            foo.usePersonAsArgument(person);
        }
    }




//    @Test
//    public void test5() {
//        // the AST should first look for  XX.person; if not then look for person in the getField Scope
//        org.instrumentor.codeexamples.Person __ins_v1 = null;
//        Foo foo = new Foo().foo();
//        Person person;
//        __ins_v1 = foo.person;
//        foo.usePersonAsArgument(__ins_v1);
//        org.helper.Assertions.verify("var.shortOpts_1", __ins_v1);
//    }

//
//    @Test
//    public void test6() {
//        org.instrumentor.codeexamples.Person __ins_v1 = null;
//        Foo foo = new Foo().foo();
//        __ins_v1 = person;
//        foo.usePersonAsArgument(person);
//        foo.usePersonAsArgument(__ins_v1);
//        org.helper.Assertions.verify("var.shortOpts_1", __ins_v1);
//    }
//
//    @Test
//    public void test7() {
//        org.instrumentor.codeexamples.Person __ins_v1 = null;
//        Foo foo = new Foo().foo();
//        __ins_v1 = person;
//        foo.usePersonAsArgument(__ins_v1);
//        foo.usePersonAsArgument(person);
//        org.helper.Assertions.verify("var.shortOpts_1", __ins_v1);
//    }







}

