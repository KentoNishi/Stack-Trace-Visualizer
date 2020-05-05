import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ProgramRunner implements Runnable {

    private ProcessBuilder builder;

    public ProgramRunner(ProcessBuilder builder) {
        this.builder = builder;
    }

    public void run() {
        try {
            Process process = this.builder.start();
            InputStream os = process.getInputStream();
            Scanner scanner = new Scanner(os);
            scanner.nextLine();
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}