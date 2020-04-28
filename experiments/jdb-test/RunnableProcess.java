import java.io.IOException;
import java.io.PrintWriter;

public class RunnableProcess implements Runnable {
    public PrintWriter input;
    public Process process;

    RunnableProcess(ProcessBuilder builder) {
        try {
            this.process = builder.start();
            this.input = new PrintWriter(process.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            this.process.waitFor();
            System.out.println("");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public PrintWriter getInput() {
        return input;
    }

    public void write(String str) {
        input.write(str + "\n");
        input.flush();
    }
}