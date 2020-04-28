import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EavesdropperTester {
    public static void main(String[] args) throws IOException {
        List<String> flags = new ArrayList<String>();
        flags.add("jdb");
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        RunnableProcess runnableProcess = new RunnableProcess(builder);
        Thread processThread = new Thread(runnableProcess);
        processThread.start();
        Thread runnableReader = new Thread(new RunnableReader(runnableProcess, processThread));
        runnableReader.setDaemon(true);
        runnableReader.start();
    }
}