public class Runner {
    public static void main(String[] args) {
        System.out.println("Initializing Tracer...");
        Tracer tracer = new Tracer("C:/Users/kento/Documents/GitHub/APCS/JMCh20_5LinkedListWithTail/TestList.class");
        // ../junk/TestProgram.class
        // C:/Users/kento/Documents/GitHub/APCS/JMCh19_SafeTrade/SafeTrade.class
        tracer.getTrace();
    }
}