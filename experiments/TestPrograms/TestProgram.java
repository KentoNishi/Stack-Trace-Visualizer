import java.util.Scanner;

public class TestProgram {
    public static void main(String[] args) {
        TestClass test = new TestClass();
        test.printHello();
        Scanner scanner = new Scanner(System.in);
        System.out.println("What is your name?");
        System.out.println("Hello " + scanner.nextLine());
        test.printBye();
    }
}