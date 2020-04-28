public class TestClass {
    public int testField;

    TestClass() {
        testField = 0;
    }

    public void printHello() {
        System.out.println("Hello");
        this.printBye();
    }

    public void printBye() {
        System.out.println("Bye");
    }
}