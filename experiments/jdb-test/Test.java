import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello");
        TimeUnit.SECONDS.sleep(1);
        test();
    }

    public static void test() {
        System.out.println("Test");
        Test2 t = new Test2();
        t.hello();
    }
}