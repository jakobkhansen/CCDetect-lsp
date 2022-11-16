package TestJavaCodebase;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * Class
 */
public class Class {

    // Clone in Another.main
    public static void main(String[] args) {
        String hello = "Hello world";
        int x = 10;
        double y = 10.3;
        Range range = new Range(new Position(0, 0), new Position(1, 7));
        System.out.println("x: " + x + ", y: " + y + ", hello: " + hello + ", range: " + range);
    }
}
