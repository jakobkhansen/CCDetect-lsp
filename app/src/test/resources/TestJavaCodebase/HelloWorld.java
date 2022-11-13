package TestJavaCodebase;

/**
 * HelloWorld
 */
public class HelloWorld {

    // Not clone
    public static void helloWorld() {
        System.out.println("Hello World!");
    }

    // Not clone
    public static void main(String[] args) {
        helloWorld();
    }

    // Not clone
    public int fibonacci(int n) {
        int x = 1, y = 1, z = 1;

        for (int i = 0; i < n; i++) {
            z = x + y;
            x = y;
            y = z;
        }

        return z;
    }

    // Not clone
    public int fibonacciWithPrint(int n) {
        int x = 1, y = 1, z = 1;

        for (int i = 0; i < n; i++) {
            z = x + y;
            System.out.println(z);
            x = y;
            y = z;
        }

        return z;
    }
}
