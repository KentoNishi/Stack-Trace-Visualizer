import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * A runnable container to run a process on a separate thread.
 */
public class ProgramRunner implements Runnable {

    private ProcessBuilder builder;

    /**
     * The ProgramRunner constructor.
     * 
     * @param builder the process builder for the thread.
     */
    public ProgramRunner(ProcessBuilder builder) {
        this.builder = builder;
    }

    /**
     * Runs the process specified in the constructor.
     */
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