public class Playground {
    public static void main(String[] args) {
        System.out.println("Initializing Tracer...");
        Tracer tracer = new Tracer("C:\\Users\\kento\\Documents\\GitHub\\APCS\\AB27_2MergeList\\MergeListTest.class");
        System.out.println("Tracing Stack...");
        String[] stackTrace = tracer.getTrace();
        System.out.println("Stack Trace:");
        for (String line : stackTrace) {
            System.out.println(line);
        }
    }
}