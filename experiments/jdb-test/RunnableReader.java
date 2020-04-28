import java.util.ArrayList;
import java.util.Scanner;

public class RunnableReader implements Runnable {
    private Scanner scanner;
    private RunnableProcess runnableProcess;
    private Thread processThread;
    private ArrayList<String> lines;

    public RunnableReader(RunnableProcess runnableProcess, Thread processThread) {
        this.processThread = processThread;
        this.scanner = new Scanner(System.in);
        this.runnableProcess = runnableProcess;
        this.lines = new ArrayList<String>();
    }

    public void run() {
        while (this.processThread.isAlive()) {
            String next = scanner.nextLine();
            this.lines.add(next);
            this.runnableProcess.write(next);
        }
    }

    public ArrayList<String> getLines() {
        return this.lines;
    }
}