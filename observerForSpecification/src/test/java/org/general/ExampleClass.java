package org.general;

public class ExampleClass extends BaseExampleClass implements MyInterface{

    public static final String CONSTANT = "constant";
    public int public_number;
    private String private_name = "Ha HY";

    private String[] arrayString = new String[10];

    private ExampleClass anotherExampleClass;

    public Object x;

    public String[] getArrayString() {
        return arrayString;
    }

    public ExampleClass getAnotherExampleClass() {
        return anotherExampleClass;
    }

    public ExampleClass(ExampleClass e) {
        this.anotherExampleClass = e;
    }

    public ExampleClass() {
        this.anotherExampleClass = null;
    }

    public static ExampleClass getExampleClass() {
        return new ExampleClass();
    }

    public String getX() {
        return "x";
    }

    public void doSomething() {
    }

    public Object getNull() {
        return null;
    }

    public int getNumber() {
        return 0;
    }

    public int getOne() {
        // this is used to separate from the parent class' implementation
        return 11;
    }

    private double getDoubleMul(double x) {
        return 0.0 * x;
    }

    private double getDoubleZero() {
        return 0.0;
    }

    public String getNameWithPara(String x) {
        return private_name;
    }

    public String getName() {
        return private_name;
    }

    public String getExtendedName(String new_content) {
        return private_name + new_content;
    }
}
