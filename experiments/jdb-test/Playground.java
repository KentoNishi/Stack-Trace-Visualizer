public class Playground {
    public static void main(String[] args) {
        // java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y TestProgram
        // jdb -connect com.sun.jdi.SocketAttach:hostname=localhost,port=8000
        System.out.println("Initializing Tracer...");
        Tracer tracer = new Tracer("../TestPrograms/TestProgram.class");
        // C:/Users/kento/Documents/GitHub/APCS/JMCh19_SafeTrade/SafeTrade.class
        StackEvent[] stackTrace = tracer.getTrace("cli");
        System.out.println("Stack Trace:\n");
        for (StackEvent line : stackTrace) {
            System.out.println(line);
        }
    }
}