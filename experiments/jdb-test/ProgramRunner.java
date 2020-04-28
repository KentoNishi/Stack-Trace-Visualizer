import java.io.IOException;

public class ProgramRunner implements Runnable {

    private ProcessBuilder builder;

    ProgramRunner(ProcessBuilder builder) {
        this.builder = builder;
    }

    public void run() {
        try {
            try {
                this.builder.start().waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}