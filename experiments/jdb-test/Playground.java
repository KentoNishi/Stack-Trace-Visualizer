
public class Playground {
    public static void main(String[] args) {
        Runner runner = new Runner("C:\\Users\\kento\\Documents\\GitHub\\APCS\\AB27_2MergeList\\MergeListTest.class");
        for (String line : runner.getTrace()) {
            System.out.println(line);
        }
    }
}