import java.util.Scanner;

public class RunnableReader implements Runnable {
    Scanner scanner;
    RunnableProcess runnableProcess;
    Thread processThread;

    public RunnableReader(RunnableProcess runnableProcess, Thread processThread) {
        this.processThread = processThread;
        this.scanner = new Scanner(System.in);
        this.runnableProcess = runnableProcess;
    }

    public void run() {
        while (this.processThread.isAlive()) {
            String next = scanner.nextLine();
            this.runnableProcess.write(next);
        }
    }
}