public class Playground {
    public static void main(String[] args) {
        System.out.println("Initializing Tracer...");
        Tracer tracer = new Tracer("../TestPrograms/TestProgram.class");
        // ../TestPrograms/TestProgram.class
        // C:/Users/kento/Documents/GitHub/APCS/JMCh19_SafeTrade/SafeTrade.class
        StackEvent[] stackTrace = tracer.getTrace();
        System.out.println("Stack Trace:");
        for (StackEvent line : stackTrace) {
            System.out.println(line);
        }
    }
}