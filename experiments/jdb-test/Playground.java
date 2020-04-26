public class Playground {
    public static void main(String[] args) {
        // To compile all: javac -sourcepath . *.java
        System.out.println("Initializing Tracer...");
        Tracer tracer = new Tracer("TestProgram.class");
        System.out.println("Tracing Stack...");
        String[] stackTrace = tracer.getTrace();
        System.out.println("Stack Trace:");
        for (String line : stackTrace) {
            System.out.println(line);
        }
    }
}