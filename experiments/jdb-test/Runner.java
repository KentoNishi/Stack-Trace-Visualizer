public class Runner {
    public static void main(String[] args) {
        System.out.println("Initializing Tracer...");
        Tracer tracer = new Tracer("../TestPrograms/TestProgram.class");
        // ../TestPrograms/TestProgram.class
        // C:/Users/kento/Documents/GitHub/APCS/JMCh19_SafeTrade/SafeTrade.class
        tracer.getTrace();
    }
}