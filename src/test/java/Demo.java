class A {
    private static String fName;

    // Static block
    static {
        System.out.println("Inside static block");
        fName = "Sudip";
    }

    private String lName;

    // Instance initializer block (non-static block)
    {
        System.out.println("Inside non-static block");
        lName = "Malakar";
    }

    // Constructor
    A() {
        System.out.println("Inside constructor");
    }
}

public class Demo {
    public static void main(String[] args) {
        System.out.println("Inside main method");

        // Creating two objects
        A obj1 = new A();
        A obj2 = new A();
    }
}
